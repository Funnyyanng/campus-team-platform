package com.campus.team.service.impl;

import com.campus.team.dto.request.ApplicationRequest;
import com.campus.team.dto.response.ApplicationResponse;
import com.campus.team.entity.Team;
import com.campus.team.entity.TeamApplication;
import com.campus.team.entity.User;
import com.campus.team.entity.Competition;
import com.campus.team.entity.Skill;
import com.campus.team.exception.BusinessException;
import com.campus.team.repository.TeamApplicationRepository;
import com.campus.team.repository.TeamRepository;
import com.campus.team.repository.UserRepository;
import com.campus.team.repository.CompetitionRepository;
import com.campus.team.repository.SkillRepository;
import com.campus.team.service.IApplicationService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ApplicationServiceImpl implements IApplicationService {
    
    @Autowired
    private TeamApplicationRepository applicationRepository;
    
    @Autowired
    private TeamRepository teamRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private CompetitionRepository competitionRepository;
    
    @Autowired
    private SkillRepository skillRepository;
    
    @Autowired
    private com.campus.team.utils.SensitiveWordFilter sensitiveWordFilter;
    
    @Autowired
    private com.campus.team.service.IMessageService messageService;
    
    @Override
    @Transactional
    public void applyToTeam(Long userId, ApplicationRequest request) {
        // 敏感词检测
        if (request.getMessage() != null && sensitiveWordFilter.containsSensitiveWord(request.getMessage())) {
            throw new BusinessException("申请留言包含不当词汇，请修改后重试");
        }
        if (request.getAnswers() != null && sensitiveWordFilter.containsSensitiveWord(request.getAnswers())) {
            throw new BusinessException("问题回答包含不当词汇，请修改后重试");
        }
        
        // 验证用户信息是否完整
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        
        long skillCount = skillRepository.findByUserId(userId).size();
        
        if (user.getMajor() == null || user.getMajor().isEmpty() ||
            user.getGrade() == null || user.getGrade().isEmpty() ||
            skillCount == 0) {
            throw new BusinessException("请先完善个人信息（专业、年级、技能标签）后再申请");
        }
        
        // 验证队伍是否存在
        Team team = teamRepository.findById(request.getTeamId())
                .orElseThrow(() -> new BusinessException("队伍不存在"));
        
        // 验证队伍是否在招募中
        if (!"RECRUITING".equals(team.getStatus())) {
            throw new BusinessException("该队伍未在招募中");
        }
        
        // 验证队伍是否已满
        if (team.getCurrentSize() >= team.getTargetSize()) {
            throw new BusinessException("该队伍已满员");
        }
        
        // 验证是否是队长本人
        if (team.getLeaderId().equals(userId)) {
            throw new BusinessException("不能申请加入自己的队伍");
        }
        
        // 验证是否已经申请过（待审核状态）
        if (applicationRepository.existsByTeamIdAndApplicantIdAndStatus(
                request.getTeamId(), userId, "PENDING")) {
            throw new BusinessException("您已经申请过该队伍，请等待审核");
        }
        
        // 验证待审核申请数量（最多5个）
        long pendingCount = applicationRepository.countByApplicantIdAndStatus(userId, "PENDING");
        if (pendingCount >= 5) {
            throw new BusinessException("您的待审核申请已达上限（5个），请等待审核后再申请");
        }
        
        // 获取用户的所有申请记录（用于互斥检测和冷却机制）
        List<TeamApplication> userApplications = applicationRepository.findByApplicantId(userId);
        
        // 互斥检测：同一竞赛不可申请多个队伍（仅对竞赛模式生效）
        if ("COMPETITION".equals(team.getType()) && team.getCompetitionId() != null) {
            Long competitionId = team.getCompetitionId();
            boolean hasAppliedToSameCompetition = userApplications.stream()
                    .filter(app -> "PENDING".equals(app.getStatus()))
                    .anyMatch(app -> {
                        Team t = teamRepository.findById(app.getTeamId()).orElse(null);
                        return t != null && "COMPETITION".equals(t.getType()) && 
                               t.getCompetitionId() != null && t.getCompetitionId().equals(competitionId);
                    });
            
            if (hasAppliedToSameCompetition) {
                throw new BusinessException("您已经申请了该竞赛的其他队伍，请等待审核");
            }
        }
        
        // 冷却机制：被拒后3天内不可再申请同一队伍
        LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3);
        boolean hasRecentRejection = userApplications.stream()
                .filter(app -> "REJECTED".equals(app.getStatus()))
                .filter(app -> app.getTeamId().equals(request.getTeamId()))
                .anyMatch(app -> app.getUpdateTime().isAfter(threeDaysAgo));
        
        if (hasRecentRejection) {
            throw new BusinessException("您的申请被拒绝后需要等待3天才能再次申请该队伍");
        }
        
        // 创建申请
        TeamApplication application = new TeamApplication();
        application.setTeamId(request.getTeamId());
        application.setApplicantId(userId);
        application.setAnswers(request.getAnswers());
        application.setMessage(request.getMessage());
        application.setStatus("PENDING");
        
        applicationRepository.save(application);
    }
    
    @Override
    public List<ApplicationResponse> getMyApplications(Long userId) {
        List<TeamApplication> applications = applicationRepository.findByApplicantId(userId);
        return applications.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<ApplicationResponse> getTeamApplications(Long userId, Long teamId) {
        // 验证队伍是否存在
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new BusinessException("队伍不存在"));
        
        // 验证是否是队长
        if (!team.getLeaderId().equals(userId)) {
            throw new BusinessException("只有队长可以查看申请");
        }
        
        List<TeamApplication> applications = applicationRepository.findByTeamId(teamId);
        return applications.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<ApplicationResponse> getTeamMembers(Long userId, Long teamId) {
        // 验证队伍是否存在
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new BusinessException("队伍不存在"));
        
        // 验证是否是队长或队员
        boolean isLeader = team.getLeaderId().equals(userId);
        boolean isMember = applicationRepository.existsByTeamIdAndApplicantIdAndStatus(teamId, userId, "APPROVED");
        
        if (!isLeader && !isMember) {
            throw new BusinessException("只有队伍成员可以查看成员列表");
        }
        
        // 获取所有已通过的申请（即队伍成员）
        List<TeamApplication> approvedApplications = applicationRepository.findByTeamId(teamId).stream()
                .filter(app -> "APPROVED".equals(app.getStatus()))
                .collect(Collectors.toList());
        
        return approvedApplications.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    private ApplicationResponse convertToResponse(TeamApplication application) {
        ApplicationResponse response = new ApplicationResponse();
        BeanUtils.copyProperties(application, response);
        
        // 获取队伍信息
        teamRepository.findById(application.getTeamId()).ifPresent(team -> {
            response.setTeamTitle(team.getTitle());
            
            // 只有竞赛模式且 competitionId 不为空时才查询竞赛信息
            if ("COMPETITION".equals(team.getType()) && team.getCompetitionId() != null) {
                competitionRepository.findById(team.getCompetitionId()).ifPresent(competition -> {
                    response.setCompetitionName(competition.getName());
                });
            }
        });
        
        // 获取申请人信息
        userRepository.findById(application.getApplicantId()).ifPresent(user -> {
            response.setApplicantName(user.getNickname());
            response.setApplicantAvatar(user.getAvatar());
            response.setApplicantCollege(user.getCollege());
            response.setApplicantMajor(user.getMajor());
            response.setApplicantGrade(user.getGrade());
            response.setApplicantWechat(user.getWechat());
            response.setApplicantQq(user.getQq());
            
            // 获取技能标签
            String skills = skillRepository.findByUserId(user.getId())
                    .stream()
                    .map(Skill::getName)
                    .collect(Collectors.joining(","));
            response.setApplicantSkills(skills);
        });
        
        return response;
    }
    
    @Override
    @Transactional
    public void cancelApplication(Long userId, Long applicationId) {
        // 查找申请
        TeamApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new BusinessException("申请不存在"));
        
        // 验证是否是申请人本人
        if (!application.getApplicantId().equals(userId)) {
            throw new BusinessException("只能撤销自己的申请");
        }
        
        // 验证申请状态
        if (!"PENDING".equals(application.getStatus())) {
            throw new BusinessException("只能撤销待审核的申请");
        }
        
        // 删除申请
        applicationRepository.delete(application);
    }
    
    @Override
    @Transactional
    public void approveApplication(Long userId, Long applicationId) {
        // 查找申请
        TeamApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new BusinessException("申请不存在"));
        
        // 查找队伍
        Team team = teamRepository.findById(application.getTeamId())
                .orElseThrow(() -> new BusinessException("队伍不存在"));
        
        // 验证是否是队长
        if (!team.getLeaderId().equals(userId)) {
            throw new BusinessException("只有队长可以审核申请");
        }
        
        // 验证申请状态
        if (!"PENDING".equals(application.getStatus())) {
            throw new BusinessException("该申请已被处理");
        }
        
        // 验证队伍是否已满
        if (team.getCurrentSize() >= team.getTargetSize()) {
            throw new BusinessException("队伍已满员，无法通过申请");
        }
        
        // 更新申请状态
        application.setStatus("APPROVED");
        applicationRepository.save(application);
        
        // 更新队伍人数
        team.setCurrentSize(team.getCurrentSize() + 1);
        
        // 如果达到目标人数，自动关闭招募
        if (team.getCurrentSize() >= team.getTargetSize()) {
            team.setStatus("FULL");
        }
        
        teamRepository.save(team);
        
        // 发送站内信通知申请人
        messageService.sendSystemMessage(
            application.getApplicantId(),
            "申请已通过",
            String.format("恭喜！您的申请已通过，已成功加入队伍「%s」", team.getTitle()),
            "APPLICATION",
            application.getTeamId()
        );
    }
    
    @Override
    @Transactional
    public void rejectApplication(Long userId, Long applicationId, String reason) {
        // 查找申请
        TeamApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new BusinessException("申请不存在"));
        
        // 查找队伍
        Team team = teamRepository.findById(application.getTeamId())
                .orElseThrow(() -> new BusinessException("队伍不存在"));
        
        // 验证是否是队长
        if (!team.getLeaderId().equals(userId)) {
            throw new BusinessException("只有队长可以审核申请");
        }
        
        // 验证申请状态
        if (!"PENDING".equals(application.getStatus())) {
            throw new BusinessException("该申请已被处理");
        }
        
        // 验证拒绝理由
        if (reason == null || reason.trim().isEmpty()) {
            throw new BusinessException("请填写拒绝理由");
        }
        
        // 更新申请状态
        application.setStatus("REJECTED");
        application.setRejectReason(reason);
        applicationRepository.save(application);
        
        // 发送站内信通知申请人
        messageService.sendSystemMessage(
            application.getApplicantId(),
            "申请被拒绝",
            String.format("很抱歉，您申请加入队伍「%s」被拒绝\n\n拒绝理由：%s", team.getTitle(), reason),
            "APPLICATION",
            application.getTeamId()
        );
    }
    
    @Override
    @Transactional
    public void batchRejectApplications(Long userId, Long teamId, String reason) {
        // 查找队伍
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new BusinessException("队伍不存在"));
        
        // 验证是否是队长
        if (!team.getLeaderId().equals(userId)) {
            throw new BusinessException("只有队长可以审核申请");
        }
        
        // 验证拒绝理由
        if (reason == null || reason.trim().isEmpty()) {
            throw new BusinessException("请填写拒绝理由");
        }
        
        // 查找该队伍所有待审核的申请
        List<TeamApplication> applications = applicationRepository.findByTeamId(teamId).stream()
                .filter(app -> "PENDING".equals(app.getStatus()))
                .toList();
        
        if (applications.isEmpty()) {
            throw new BusinessException("没有待审核的申请");
        }
        
        // 批量更新状态
        for (TeamApplication application : applications) {
            application.setStatus("REJECTED");
            application.setRejectReason(reason);
        }
        
        applicationRepository.saveAll(applications);
    }
}
