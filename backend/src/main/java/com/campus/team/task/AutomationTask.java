package com.campus.team.task;

import com.campus.team.entity.Competition;
import com.campus.team.entity.Team;
import com.campus.team.entity.TeamApplication;
import com.campus.team.repository.CompetitionRepository;
import com.campus.team.repository.TeamApplicationRepository;
import com.campus.team.repository.TeamRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
public class AutomationTask {
    
    @Autowired
    private TeamApplicationRepository applicationRepository;
    
    @Autowired
    private TeamRepository teamRepository;
    
    @Autowired
    private CompetitionRepository competitionRepository;
    
    /**
     * 申请超时失效
     * 每天凌晨2点执行，将7天未处理的申请标记为失效
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void expireApplications() {
        log.info("开始执行申请超时失效任务");
        
        try {
            // 计算7天前的时间
            LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
            
            // 查找所有待审核且创建时间超过7天的申请
            List<TeamApplication> applications = applicationRepository.findAll().stream()
                    .filter(app -> "PENDING".equals(app.getStatus()))
                    .filter(app -> app.getCreateTime().isBefore(sevenDaysAgo))
                    .toList();
            
            if (applications.isEmpty()) {
                log.info("没有需要失效的申请");
                return;
            }
            
            // 批量更新状态为已拒绝
            for (TeamApplication application : applications) {
                application.setStatus("REJECTED");
                application.setRejectReason("申请超时未处理，已自动失效");
            }
            
            applicationRepository.saveAll(applications);
            
            log.info("申请超时失效任务完成，共处理 {} 条申请", applications.size());
        } catch (Exception e) {
            log.error("申请超时失效任务执行失败", e);
        }
    }
    
    /**
     * 自动满员
     * 每小时执行一次，检查队伍人数是否达到目标，自动关闭招募
     */
    @Scheduled(cron = "0 0 * * * ?")
    @Transactional
    public void autoCloseFullTeams() {
        log.info("开始执行自动满员任务");
        
        try {
            // 查找所有招募中且已满员的队伍
            List<Team> teams = teamRepository.findByStatus("RECRUITING").stream()
                    .filter(team -> team.getCurrentSize() >= team.getTargetSize())
                    .toList();
            
            if (teams.isEmpty()) {
                log.info("没有需要关闭的队伍");
                return;
            }
            
            // 批量更新状态为已满员
            for (Team team : teams) {
                team.setStatus("FULL");
            }
            
            teamRepository.saveAll(teams);
            
            log.info("自动满员任务完成，共关闭 {} 支队伍", teams.size());
        } catch (Exception e) {
            log.error("自动满员任务执行失败", e);
        }
    }
    
    /**
     * 竞赛自动过期
     * 每天凌晨3点执行，将报名截止的竞赛标记为已结束
     */
    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional
    public void expireCompetitions() {
        log.info("开始执行竞赛自动过期任务");
        
        try {
            LocalDateTime now = LocalDateTime.now();
            
            // 查找所有进行中但报名已截止的竞赛
            List<Competition> competitions = competitionRepository.findAll().stream()
                    .filter(comp -> "ACTIVE".equals(comp.getStatus()))
                    .filter(comp -> comp.getRegistrationEnd() != null)
                    .filter(comp -> comp.getRegistrationEnd().isBefore(now))
                    .toList();
            
            if (competitions.isEmpty()) {
                log.info("没有需要过期的竞赛");
                return;
            }
            
            // 批量更新状态为已结束
            for (Competition competition : competitions) {
                competition.setStatus("ENDED");
            }
            
            competitionRepository.saveAll(competitions);
            
            // 同时关闭这些竞赛下的所有招募中队伍
            for (Competition competition : competitions) {
                List<Team> teams = teamRepository.findByCompetitionIdAndStatus(
                        competition.getId(), "RECRUITING");
                
                for (Team team : teams) {
                    team.setStatus("CLOSED");
                }
                
                if (!teams.isEmpty()) {
                    teamRepository.saveAll(teams);
                }
            }
            
            log.info("竞赛自动过期任务完成，共处理 {} 个竞赛", competitions.size());
        } catch (Exception e) {
            log.error("竞赛自动过期任务执行失败", e);
        }
    }
}
