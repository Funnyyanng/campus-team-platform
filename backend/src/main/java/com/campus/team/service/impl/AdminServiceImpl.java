package com.campus.team.service.impl;

import com.campus.team.common.PageResult;
import com.campus.team.dto.request.BanUserRequest;
import com.campus.team.dto.request.UserQueryRequest;
import com.campus.team.dto.response.AnnouncementResponse;
import com.campus.team.dto.response.DashboardChartsResponse;
import com.campus.team.dto.response.DashboardStatsResponse;
import com.campus.team.dto.response.TeamResponse;
import com.campus.team.dto.response.UserManageResponse;
import com.campus.team.entity.Announcement;
import com.campus.team.entity.Competition;
import com.campus.team.entity.Team;
import com.campus.team.entity.User;
import com.campus.team.exception.BusinessException;
import com.campus.team.repository.AnnouncementRepository;
import com.campus.team.repository.CompetitionRepository;
import com.campus.team.repository.SkillRepository;
import com.campus.team.repository.TeamRepository;
import com.campus.team.repository.UserRepository;
import com.campus.team.service.IAdminService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AdminServiceImpl implements IAdminService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private AnnouncementRepository announcementRepository;
    
    @Autowired
    private TeamRepository teamRepository;
    
    @Autowired
    private CompetitionRepository competitionRepository;
    
    @Autowired
    private SkillRepository skillRepository;
    
    @Autowired
    private com.campus.team.repository.TeamApplicationRepository teamApplicationRepository;
    
    @Autowired
    private com.campus.team.service.IMessageService messageService;
    
    @Override
    public PageResult<UserManageResponse> getUserList(Long adminId, UserQueryRequest queryRequest) {
        // 验证管理员权限
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        
        if (!"ADMIN".equals(admin.getRole())) {
            throw new BusinessException("无权限访问");
        }
        
        // 构建查询条件
        Specification<User> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // 关键词搜索（邮箱或昵称）
            if (queryRequest.getKeyword() != null && !queryRequest.getKeyword().isEmpty()) {
                String keyword = "%" + queryRequest.getKeyword() + "%";
                predicates.add(cb.or(
                    cb.like(root.get("email"), keyword),
                    cb.like(root.get("nickname"), keyword)
                ));
            }
            
            // 状态筛选
            if (queryRequest.getStatus() != null && !queryRequest.getStatus().isEmpty()) {
                predicates.add(cb.equal(root.get("status"), queryRequest.getStatus()));
            }
            
            // 角色筛选
            if (queryRequest.getRole() != null && !queryRequest.getRole().isEmpty()) {
                predicates.add(cb.equal(root.get("role"), queryRequest.getRole()));
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        
        // 分页查询
        Pageable pageable = PageRequest.of(
            queryRequest.getPage() - 1,
            queryRequest.getPageSize(),
            Sort.by(Sort.Direction.DESC, "createTime")
        );
        
        Page<User> userPage = userRepository.findAll(spec, pageable);
        
        // 转换为响应对象
        List<UserManageResponse> records = userPage.getContent().stream()
                .map(user -> {
                    UserManageResponse response = new UserManageResponse();
                    BeanUtils.copyProperties(user, response);
                    return response;
                })
                .collect(Collectors.toList());
        
        return new PageResult<>(
            records,
            userPage.getTotalElements(),
            queryRequest.getPage(),
            queryRequest.getPageSize()
        );
    }
    
    @Override
    @Transactional
    public void banUser(Long adminId, Long userId, BanUserRequest request) {
        // 验证管理员权限
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        
        if (!"ADMIN".equals(admin.getRole())) {
            throw new BusinessException("无权限操作");
        }
        
        // 不能封禁自己
        if (adminId.equals(userId)) {
            throw new BusinessException("不能封禁自己");
        }
        
        // 查找目标用户
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("目标用户不存在"));
        
        // 不能封禁其他管理员
        if ("ADMIN".equals(user.getRole())) {
            throw new BusinessException("不能封禁管理员");
        }
        
        // 更新状态
        user.setStatus(request.getStatus());
        userRepository.save(user);
        
        log.info("管理员 {} 将用户 {} 状态修改为: {}, 原因: {}", 
                adminId, userId, request.getStatus(), request.getReason());
    }
    
    @Override
    @Transactional
    public void deleteUser(Long adminId, Long userId) {
        // 验证管理员权限
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        
        if (!"ADMIN".equals(admin.getRole())) {
            throw new BusinessException("无权限操作");
        }
        
        // 不能删除自己
        if (adminId.equals(userId)) {
            throw new BusinessException("不能删除自己");
        }
        
        // 查找目标用户
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("目标用户不存在"));
        
        // 不能删除其他管理员
        if ("ADMIN".equals(user.getRole())) {
            throw new BusinessException("不能删除管理员");
        }
        
        // 删除用户
        userRepository.delete(user);
        
        log.info("管理员 {} 删除了用户 {}", adminId, userId);
    }
    
    @Override
    @Transactional
    public void publishAnnouncement(Long adminId, String title, String content) {
        // 验证管理员权限
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        
        if (!"ADMIN".equals(admin.getRole())) {
            throw new BusinessException("无权限操作");
        }
        
        // 验证公告内容
        if (title == null || title.trim().isEmpty()) {
            throw new BusinessException("公告标题不能为空");
        }
        
        if (content == null || content.trim().isEmpty()) {
            throw new BusinessException("公告内容不能为空");
        }
        
        if (title.length() > 100) {
            throw new BusinessException("公告标题不能超过100个字符");
        }
        
        if (content.length() > 2000) {
            throw new BusinessException("公告内容不能超过2000个字符");
        }
        
        // 获取所有正常状态的用户
        List<User> allUsers = userRepository.findAll().stream()
                .filter(user -> "NORMAL".equals(user.getStatus()))
                .collect(Collectors.toList());
        
        // 通过MessageService发送公告给所有用户
        int sentCount = 0;
        for (User user : allUsers) {
            try {
                messageService.sendSystemMessage(
                    user.getId(),
                    "【系统公告】" + title,
                    content,
                    "SYSTEM",
                    null
                );
                sentCount++;
            } catch (Exception e) {
                log.error("发送公告给用户 {} 失败：{}", user.getId(), e.getMessage());
            }
        }
        
        // 保存公告记录到announcements表
        Announcement announcement = new Announcement();
        announcement.setTitle(title);
        announcement.setContent(content);
        announcement.setPublisherId(adminId);
        announcement.setPublisherName(admin.getNickname());
        announcement.setRecipientCount(sentCount);
        announcementRepository.save(announcement);
        
        log.info("管理员 {} 发布了公告：{}，共发送给 {}/{} 个用户", 
                adminId, title, sentCount, allUsers.size());
    }
    
    @Override
    public List<AnnouncementResponse> getAnnouncementHistory(Long adminId) {
        // 验证管理员权限
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        
        if (!"ADMIN".equals(admin.getRole())) {
            throw new BusinessException("无权限访问");
        }
        
        // 查询所有公告，按时间倒序
        List<Announcement> announcements = announcementRepository.findAllByOrderByCreateTimeDesc();
        
        // 转换为响应对象
        return announcements.stream()
                .map(announcement -> {
                    AnnouncementResponse response = new AnnouncementResponse();
                    BeanUtils.copyProperties(announcement, response);
                    return response;
                })
                .collect(Collectors.toList());
    }
    
    @Override
    public DashboardStatsResponse getDashboardStats(Long adminId) {
        // 验证管理员权限
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        
        if (!"ADMIN".equals(admin.getRole())) {
            throw new BusinessException("无权限访问");
        }
        
        DashboardStatsResponse stats = new DashboardStatsResponse();
        
        // 统计总用户数
        stats.setTotalUsers(userRepository.count());
        
        // 统计活跃队伍数（招募中的队伍）
        stats.setActiveTeams(teamRepository.countByStatus("RECRUITING"));
        
        // 统计进行中的竞赛数
        stats.setActiveCompetitions(competitionRepository.countByStatus("ACTIVE"));
        
        return stats;
    }
    
    @Override
    public DashboardChartsResponse getDashboardCharts(Long adminId) {
        // 验证管理员权限
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        
        if (!"ADMIN".equals(admin.getRole())) {
            throw new BusinessException("无权限访问");
        }
        
        DashboardChartsResponse charts = new DashboardChartsResponse();
        
        // 队伍状态分布
        DashboardChartsResponse.TeamStatusData teamStatus = new DashboardChartsResponse.TeamStatusData();
        teamStatus.setRecruiting(teamRepository.countByStatus("RECRUITING"));
        teamStatus.setFull(teamRepository.countByStatus("FULL"));
        teamStatus.setClosed(teamRepository.countByStatus("CLOSED"));
        charts.setTeamStatus(teamStatus);
        
        // 竞赛级别分布
        DashboardChartsResponse.CompetitionLevelData competitionLevel = new DashboardChartsResponse.CompetitionLevelData();
        competitionLevel.setNational(competitionRepository.countByLevel("国家级"));
        competitionLevel.setProvincial(competitionRepository.countByLevel("省级"));
        competitionLevel.setSchool(competitionRepository.countByLevel("校级"));
        charts.setCompetitionLevel(competitionLevel);
        
        // 热门技能标签统计（TOP 10）
        DashboardChartsResponse.SkillTagData skillTag = new DashboardChartsResponse.SkillTagData();
        try {
            List<Object[]> skillStats = skillRepository.findTopSkills(10);
            List<DashboardChartsResponse.SkillTagItem> skillItems = skillStats.stream()
                    .map(stat -> {
                        DashboardChartsResponse.SkillTagItem item = new DashboardChartsResponse.SkillTagItem();
                        item.setName((String) stat[0]);
                        item.setCount(((Number) stat[1]).longValue());
                        return item;
                    })
                    .collect(Collectors.toList());
            skillTag.setItems(skillItems);
        } catch (Exception e) {
            // 如果skills表不存在，返回空列表
            log.warn("无法查询技能标签统计，可能skills表不存在: {}", e.getMessage());
            skillTag.setItems(new ArrayList<>());
        }
        charts.setSkillTag(skillTag);
        
        // 队伍类型分布
        DashboardChartsResponse.TeamTypeData teamType = new DashboardChartsResponse.TeamTypeData();
        teamType.setCompetition(teamRepository.countByType("COMPETITION"));
        teamType.setProject(teamRepository.countByType("PROJECT"));
        charts.setTeamType(teamType);
        
        return charts;
    }
    
    @Override
    public List<TeamResponse> getAllTeams(Long adminId) {
        // 验证管理员权限
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        
        if (!"ADMIN".equals(admin.getRole())) {
            throw new BusinessException("无权限访问");
        }
        
        // 获取所有队伍（不限状态）
        List<Team> teams = teamRepository.findAll(Sort.by(Sort.Direction.DESC, "createTime"));
        
        return teams.stream()
                .map(team -> {
                    TeamResponse response = new TeamResponse();
                    BeanUtils.copyProperties(team, response);
                    
                    // 设置队长信息
                    User leader = userRepository.findById(team.getLeaderId()).orElse(null);
                    if (leader != null) {
                        response.setLeaderName(leader.getNickname());
                        response.setLeaderAvatar(leader.getAvatar());
                    }
                    
                    // 设置竞赛名称（如果是竞赛队伍）
                    if ("COMPETITION".equals(team.getType()) && team.getCompetitionId() != null) {
                        Competition competition = competitionRepository.findById(team.getCompetitionId()).orElse(null);
                        if (competition != null) {
                            response.setCompetitionName(competition.getName());
                        }
                    }
                    
                    // 统计待审核申请数量
                    // 这里可以添加统计逻辑，暂时返回0
                    response.setPendingApplicationCount(0L);
                    
                    return response;
                })
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public void deleteTeam(Long adminId, Long teamId, String reason) {
        // 验证管理员权限
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        
        if (!"ADMIN".equals(admin.getRole())) {
            throw new BusinessException("无权限操作");
        }
        
        // 获取队伍信息
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new BusinessException("队伍不存在"));
        
        // 获取所有队员（包括队长）
        List<com.campus.team.entity.TeamApplication> applications = 
                teamApplicationRepository.findByTeamId(teamId);
        
        // 收集所有已通过审核的队员ID
        List<Long> memberIds = applications.stream()
                .filter(app -> "APPROVED".equals(app.getStatus()))
                .map(com.campus.team.entity.TeamApplication::getApplicantId)
                .collect(Collectors.toList());
        
        // 添加队长ID
        if (!memberIds.contains(team.getLeaderId())) {
            memberIds.add(team.getLeaderId());
        }
        
        // 发送站内信通知所有成员
        String title = "【系统通知】队伍已被管理员删除";
        String content = String.format(
                "你所在的队伍「%s」已被管理员删除。\n\n删除原因：%s\n\n如有疑问，请联系管理员。",
                team.getTitle(),
                reason != null && !reason.trim().isEmpty() ? reason : "未提供"
        );
        
        for (Long memberId : memberIds) {
            messageService.sendSystemMessage(memberId, title, content, "TEAM", teamId);
        }
        
        // 删除队伍（会级联删除申请记录）
        teamRepository.delete(team);
        
        log.info("管理员 {} 删除了队伍 {}，原因：{}", adminId, teamId, reason);
    }
}
