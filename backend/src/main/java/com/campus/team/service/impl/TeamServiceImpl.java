package com.campus.team.service.impl;

import com.campus.team.dto.request.TeamCreateRequest;
import com.campus.team.dto.response.TeamResponse;
import com.campus.team.entity.Competition;
import com.campus.team.entity.Team;
import com.campus.team.entity.User;
import com.campus.team.entity.TeamApplication;
import com.campus.team.exception.BusinessException;
import com.campus.team.repository.CompetitionRepository;
import com.campus.team.repository.TeamRepository;
import com.campus.team.repository.UserRepository;
import com.campus.team.repository.SkillRepository;
import com.campus.team.repository.TeamApplicationRepository;
import com.campus.team.service.ITeamService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TeamServiceImpl implements ITeamService {
    
    @Autowired
    private TeamRepository teamRepository;
    
    @Autowired
    private CompetitionRepository competitionRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private SkillRepository skillRepository;
    
    @Autowired
    private TeamApplicationRepository applicationRepository;
    
    @Autowired
    private com.campus.team.utils.SensitiveWordFilter sensitiveWordFilter;
    
    @Autowired
    private com.campus.team.service.IMessageService messageService;
    
    @Override
    @Transactional
    public void createTeam(Long userId, TeamCreateRequest request) {
        // 验证用户信息是否完整
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        
        long skillCount = skillRepository.findByUserId(userId).size();
        
        if (user.getMajor() == null || user.getMajor().isEmpty() ||
            user.getGrade() == null || user.getGrade().isEmpty() ||
            skillCount == 0) {
            throw new BusinessException("请先完善个人信息（专业、年级、技能标签）后再创建队伍");
        }
        
        // 敏感词检测
        if (sensitiveWordFilter.containsSensitiveWord(request.getTitle())) {
            throw new BusinessException("队伍标题包含不当词汇，请修改后重试");
        }
        if (sensitiveWordFilter.containsSensitiveWord(request.getDescription())) {
            throw new BusinessException("队伍简介包含不当词汇，请修改后重试");
        }
        if (request.getQuestions() != null && sensitiveWordFilter.containsSensitiveWord(request.getQuestions())) {
            throw new BusinessException("验证问题包含不当词汇，请修改后重试");
        }
        
        // 验证队伍类型
        if (request.getType() == null || 
            (!request.getType().equals("COMPETITION") && !request.getType().equals("PROJECT"))) {
            throw new BusinessException("队伍类型必须是 COMPETITION 或 PROJECT");
        }
        
        // 根据队伍类型进行不同的验证
        if ("COMPETITION".equals(request.getType())) {
            // 竞赛模式验证
            if (request.getCompetitionId() == null) {
                throw new BusinessException("竞赛模式必须选择竞赛");
            }
            
            // 验证竞赛是否存在
            Competition competition = competitionRepository.findById(request.getCompetitionId())
                    .orElseThrow(() -> new BusinessException("竞赛不存在"));
            
            // 验证竞赛是否进行中
            if (!"ACTIVE".equals(competition.getStatus())) {
                throw new BusinessException("该竞赛未开放报名");
            }
            
            // 验证用户是否已经在该竞赛中创建过队伍
            if (teamRepository.existsByCompetitionIdAndLeaderId(request.getCompetitionId(), userId)) {
                throw new BusinessException("您已经在该竞赛中创建过队伍");
            }
            
            // 验证目标人数是否符合竞赛要求
            if (request.getTargetSize() < competition.getMinTeamSize() || 
                request.getTargetSize() > competition.getMaxTeamSize()) {
                throw new BusinessException(
                    String.format("队伍人数必须在%d-%d人之间", 
                        competition.getMinTeamSize(), competition.getMaxTeamSize())
                );
            }
        } else {
            // 课题模式验证
            if (request.getProjectName() == null || request.getProjectName().trim().isEmpty()) {
                throw new BusinessException("课题模式必须填写课题名称");
            }
            
            if (request.getProjectType() == null || request.getProjectType().trim().isEmpty()) {
                throw new BusinessException("课题模式必须选择课题类型");
            }
            
            if (request.getProjectName().length() > 200) {
                throw new BusinessException("课题名称不能超过200个字符");
            }
            
            // 验证目标人数（课题模式固定2-10人）
            if (request.getTargetSize() < 2 || request.getTargetSize() > 10) {
                throw new BusinessException("课题队伍人数必须在2-10人之间");
            }
        }
        
        // 创建队伍
        Team team = new Team();
        BeanUtils.copyProperties(request, team);
        team.setLeaderId(userId);
        team.setCurrentSize(1);
        team.setStatus("RECRUITING");
        
        teamRepository.save(team);
    }
    
    @Override
    public List<TeamResponse> getAllRecruitingTeams() {
        List<Team> teams = teamRepository.findByStatus("RECRUITING");
        return teams.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<TeamResponse> getTeamsByCompetition(Long competitionId) {
        List<Team> teams = teamRepository.findByCompetitionIdAndStatus(competitionId, "RECRUITING");
        return teams.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public TeamResponse getTeamById(Long id) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new BusinessException("队伍不存在"));
        return convertToResponse(team);
    }
    
    @Override
    public List<TeamResponse> getMyTeams(Long userId) {
        // 获取用户作为队长创建的队伍
        List<Team> leaderTeams = teamRepository.findByLeaderId(userId);
        
        // 获取用户作为队员加入的队伍（通过APPROVED状态的申请）
        List<Long> memberTeamIds = applicationRepository.findByApplicantIdAndStatus(userId, "APPROVED")
                .stream()
                .map(TeamApplication::getTeamId)
                .distinct()
                .collect(Collectors.toList());
        
        List<Team> memberTeams = memberTeamIds.isEmpty() ? 
                List.of() : teamRepository.findAllById(memberTeamIds);
        
        // 合并两个列表，去重（避免队长同时也是成员的情况）
        List<Team> allTeams = new java.util.ArrayList<>(leaderTeams);
        for (Team memberTeam : memberTeams) {
            if (allTeams.stream().noneMatch(t -> t.getId().equals(memberTeam.getId()))) {
                allTeams.add(memberTeam);
            }
        }
        
        return allTeams.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public void closeRecruitment(Long userId, Long teamId) {
        // 查找队伍
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new BusinessException("队伍不存在"));
        
        // 验证是否是队长
        if (!team.getLeaderId().equals(userId)) {
            throw new BusinessException("只有队长可以关闭招募");
        }
        
        // 验证队伍状态
        if (!"RECRUITING".equals(team.getStatus())) {
            throw new BusinessException("该队伍未在招募中");
        }
        
        // 关闭招募
        team.setStatus("CLOSED");
        teamRepository.save(team);
    }
    
    @Override
    @Transactional
    public void removeMember(Long userId, Long teamId, Long memberId) {
        // 查找队伍
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new BusinessException("队伍不存在"));
        
        // 验证是否是队长
        if (!team.getLeaderId().equals(userId)) {
            throw new BusinessException("只有队长可以移除成员");
        }
        
        // 验证不能移除队长自己
        if (memberId.equals(userId)) {
            throw new BusinessException("不能移除队长自己");
        }
        
        // 验证成员是否存在
        User member = userRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException("成员不存在"));
        
        // 查找该成员的申请记录并更新状态为REMOVED
        List<TeamApplication> memberApplications = applicationRepository.findByTeamId(teamId).stream()
                .filter(app -> app.getApplicantId().equals(memberId) && "APPROVED".equals(app.getStatus()))
                .toList();
        
        if (memberApplications.isEmpty()) {
            throw new BusinessException("该用户不是队伍成员");
        }
        
        // 更新申请状态为REMOVED
        for (TeamApplication application : memberApplications) {
            application.setStatus("REMOVED");
        }
        applicationRepository.saveAll(memberApplications);
        
        // 减少队伍人数
        if (team.getCurrentSize() > 1) {
            team.setCurrentSize(team.getCurrentSize() - 1);
            
            // 如果队伍之前是满员状态，移除成员后重新开启招募
            if ("FULL".equals(team.getStatus())) {
                team.setStatus("RECRUITING");
            }
            
            teamRepository.save(team);
        } else {
            throw new BusinessException("队伍至少需要保留队长");
        }
        
        // 发送站内信通知被移除的成员
        User leader = userRepository.findById(userId).orElse(null);
        String leaderName = leader != null ? leader.getNickname() : "队长";
        messageService.sendSystemMessage(
            memberId,
            "您已被移出队伍",
            String.format("您已被 %s 从队伍「%s」中移除", leaderName, team.getTitle()),
            "TEAM",
            teamId
        );
    }
    
    @Override
    @Transactional
    public void deleteTeam(Long userId, Long teamId) {
        // 查找队伍
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new BusinessException("队伍不存在"));
        
        // 验证是否是队长
        if (!team.getLeaderId().equals(userId)) {
            throw new BusinessException("只有队长可以删除队伍");
        }
        
        // 删除队伍
        teamRepository.delete(team);
        
        // TODO: 同时删除相关的申请记录和成员记录
    }
    
    @Override
    @Transactional
    public void transferLeadership(Long userId, Long teamId, Long newLeaderId) {
        // 查找队伍
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new BusinessException("队伍不存在"));
        
        // 验证是否是队长
        if (!team.getLeaderId().equals(userId)) {
            throw new BusinessException("只有队长可以转让队长权限");
        }
        
        // 验证不能转让给自己
        if (newLeaderId.equals(userId)) {
            throw new BusinessException("不能转让给自己");
        }
        
        // 验证新队长是否存在
        User newLeader = userRepository.findById(newLeaderId)
                .orElseThrow(() -> new BusinessException("新队长不存在"));
        
        // 验证新队长是否是队伍成员
        List<TeamApplication> newLeaderApplications = applicationRepository.findByTeamId(teamId).stream()
                .filter(app -> app.getApplicantId().equals(newLeaderId) && "APPROVED".equals(app.getStatus()))
                .toList();
        
        if (newLeaderApplications.isEmpty()) {
            throw new BusinessException("只能将队长权限转让给队伍成员");
        }
        
        // 删除新队长的APPROVED申请记录（因为队长不需要申请记录）
        applicationRepository.deleteAll(newLeaderApplications);
        
        // 为原队长创建一个APPROVED申请记录，使其成为普通成员
        TeamApplication oldLeaderApplication = new TeamApplication();
        oldLeaderApplication.setTeamId(teamId);
        oldLeaderApplication.setApplicantId(userId);
        oldLeaderApplication.setStatus("APPROVED");
        oldLeaderApplication.setMessage("队长转让后自动成为队员");
        applicationRepository.save(oldLeaderApplication);
        
        // 转让队长权限
        team.setLeaderId(newLeaderId);
        teamRepository.save(team);
        
        // 注意：人数不变，因为只是角色互换，总人数没有变化
        
        // 发送站内信通知新队长
        User oldLeader = userRepository.findById(userId).orElse(null);
        String oldLeaderName = oldLeader != null ? oldLeader.getNickname() : "原队长";
        messageService.sendSystemMessage(
            newLeaderId,
            "您已成为队长",
            String.format("%s 将队伍「%s」的队长权限转让给了您，请及时管理队伍", oldLeaderName, team.getTitle()),
            "TEAM",
            teamId
        );
        
        // 发送站内信通知原队长（确认转让成功）
        messageService.sendSystemMessage(
            userId,
            "队长权限已转让",
            String.format("您已成功将队伍「%s」的队长权限转让给 %s", team.getTitle(), newLeader.getNickname()),
            "TEAM",
            teamId
        );
    }
    
    @Override
    @Transactional
    public void quitTeam(Long userId, Long teamId, String reason) {
        // 查找队伍
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new BusinessException("队伍不存在"));
        
        // 验证不能是队长
        if (team.getLeaderId().equals(userId)) {
            throw new BusinessException("队长不能直接退出队伍，请先转让队长权限或删除队伍");
        }
        
        // 验证退出理由
        if (reason == null || reason.trim().isEmpty()) {
            throw new BusinessException("请填写退出理由");
        }
        
        // 查找该成员的申请记录并更新状态为QUIT
        List<TeamApplication> memberApplications = applicationRepository.findByTeamId(teamId).stream()
                .filter(app -> app.getApplicantId().equals(userId) && "APPROVED".equals(app.getStatus()))
                .toList();
        
        if (memberApplications.isEmpty()) {
            throw new BusinessException("您不是该队伍的成员");
        }
        
        // 更新申请状态为QUIT，并记录退出理由
        for (TeamApplication application : memberApplications) {
            application.setStatus("QUIT");
            application.setRejectReason(reason); // 复用reject_reason字段存储退出理由
        }
        applicationRepository.saveAll(memberApplications);
        
        // 减少队伍人数
        if (team.getCurrentSize() > 1) {
            team.setCurrentSize(team.getCurrentSize() - 1);
            
            // 如果队伍之前是满员状态，成员退出后重新开启招募
            if ("FULL".equals(team.getStatus())) {
                team.setStatus("RECRUITING");
            }
            
            teamRepository.save(team);
        }
        
        // 发送站内信通知队长
        User member = userRepository.findById(userId).orElse(null);
        String memberName = member != null ? member.getNickname() : "队员";
        messageService.sendSystemMessage(
            team.getLeaderId(),
            "队员退出通知",
            String.format("%s 退出了队伍「%s」\n\n退出理由：%s", memberName, team.getTitle(), reason),
            "TEAM",
            teamId
        );
    }
    
    @Override
    public List<TeamResponse> getProjectTeams() {
        List<Team> teams = teamRepository.findByType("PROJECT");
        return teams.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<TeamResponse> getRecruitingProjectTeams() {
        List<Team> teams = teamRepository.findByTypeAndStatus("PROJECT", "RECRUITING");
        return teams.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public com.campus.team.dto.response.RefreshCheckResponse checkCanRefresh(Long userId, Long teamId) {
        // 查找队伍
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new BusinessException("队伍不存在"));
        
        // 验证是否是队长
        if (!team.getLeaderId().equals(userId)) {
            return new com.campus.team.dto.response.RefreshCheckResponse(
                false, "只有队长可以调整招募信息", null, null
            );
        }
        
        // 验证队伍状态
        if (!"RECRUITING".equals(team.getStatus())) {
            return new com.campus.team.dto.response.RefreshCheckResponse(
                false, "队伍未在招募中", null, null
            );
        }
        
        // 验证创建时间（3天内）
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.time.LocalDateTime createTime = team.getCreateTime();
        java.time.LocalDateTime refreshDeadline = createTime.plusDays(3);
        
        if (now.isAfter(refreshDeadline)) {
            return new com.campus.team.dto.response.RefreshCheckResponse(
                false, "已过调整期限（创建后3天内可调整）", null, refreshDeadline
            );
        }
        
        // 验证上次调整时间（24小时冷却）
        if (team.getLastRefreshTime() != null) {
            java.time.LocalDateTime nextRefreshTime = team.getLastRefreshTime().plusHours(24);
            if (now.isBefore(nextRefreshTime)) {
                long hoursLeft = java.time.Duration.between(now, nextRefreshTime).toHours();
                return new com.campus.team.dto.response.RefreshCheckResponse(
                    false, 
                    String.format("今日已调整，还需等待 %d 小时", hoursLeft), 
                    nextRefreshTime, 
                    refreshDeadline
                );
            }
        }
        
        return new com.campus.team.dto.response.RefreshCheckResponse(
            true, "可以调整", now, refreshDeadline
        );
    }
    
    @Override
    @Transactional
    public void refreshTeam(Long userId, Long teamId, com.campus.team.dto.request.TeamUpdateRequest request) {
        // 检查是否可以调整
        com.campus.team.dto.response.RefreshCheckResponse checkResult = checkCanRefresh(userId, teamId);
        if (!checkResult.getCanRefresh()) {
            throw new BusinessException(checkResult.getReason());
        }
        
        // 敏感词检测
        if (sensitiveWordFilter.containsSensitiveWord(request.getTitle())) {
            throw new BusinessException("队伍标题包含不当词汇，请修改后重试");
        }
        if (sensitiveWordFilter.containsSensitiveWord(request.getDescription())) {
            throw new BusinessException("队伍简介包含不当词汇，请修改后重试");
        }
        if (request.getQuestions() != null && sensitiveWordFilter.containsSensitiveWord(request.getQuestions())) {
            throw new BusinessException("验证问题包含不当词汇，请修改后重试");
        }
        
        // 查找队伍
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new BusinessException("队伍不存在"));
        
        // 验证目标人数（只能增加，不能减少）
        if (request.getTargetSize() < team.getCurrentSize()) {
            throw new BusinessException("目标人数不能少于当前人数");
        }
        
        // 验证目标人数范围
        if ("COMPETITION".equals(team.getType()) && team.getCompetitionId() != null) {
            Competition competition = competitionRepository.findById(team.getCompetitionId())
                    .orElseThrow(() -> new BusinessException("竞赛不存在"));
            if (request.getTargetSize() < competition.getMinTeamSize() || 
                request.getTargetSize() > competition.getMaxTeamSize()) {
                throw new BusinessException(
                    String.format("队伍人数必须在%d-%d人之间", 
                        competition.getMinTeamSize(), competition.getMaxTeamSize())
                );
            }
        } else if ("PROJECT".equals(team.getType())) {
            if (request.getTargetSize() < 2 || request.getTargetSize() > 10) {
                throw new BusinessException("课题队伍人数必须在2-10人之间");
            }
        }
        
        // 更新队伍信息
        team.setTitle(request.getTitle());
        team.setDescription(request.getDescription());
        team.setTargetSize(request.getTargetSize());
        team.setRequiredSkills(request.getRequiredSkills());
        team.setQuestions(request.getQuestions());
        team.setLastRefreshTime(java.time.LocalDateTime.now());
        
        // 如果调整后达到目标人数，自动关闭招募
        if (team.getCurrentSize() >= team.getTargetSize()) {
            team.setStatus("FULL");
        }
        
        teamRepository.save(team);
    }
    
    private TeamResponse convertToResponse(Team team) {
        TeamResponse response = new TeamResponse();
        BeanUtils.copyProperties(team, response);
        
        // 获取竞赛信息（仅竞赛模式）
        if ("COMPETITION".equals(team.getType()) && team.getCompetitionId() != null) {
            competitionRepository.findById(team.getCompetitionId()).ifPresent(competition -> {
                response.setCompetitionName(competition.getName());
            });
        }
        
        // 获取队长信息
        userRepository.findById(team.getLeaderId()).ifPresent(user -> {
            response.setLeaderName(user.getNickname());
            response.setLeaderAvatar(user.getAvatar());
        });
        
        // 统计待审核申请数量
        long pendingCount = applicationRepository.countByTeamIdAndStatus(team.getId(), "PENDING");
        response.setPendingApplicationCount(pendingCount);
        
        return response;
    }
}
