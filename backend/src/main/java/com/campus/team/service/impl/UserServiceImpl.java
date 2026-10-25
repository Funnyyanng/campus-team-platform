package com.campus.team.service.impl;

import com.campus.team.dto.request.UpdateProfileRequest;
import com.campus.team.dto.request.UserSearchRequest;
import com.campus.team.dto.response.UserProfileResponse;
import com.campus.team.dto.response.UserSearchResponse;
import com.campus.team.entity.Skill;
import com.campus.team.entity.User;
import com.campus.team.exception.BusinessException;
import com.campus.team.repository.SkillRepository;
import com.campus.team.repository.UserRepository;
import com.campus.team.service.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserServiceImpl implements IUserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private SkillRepository skillRepository;
    
    @Override
    public UserProfileResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        
        UserProfileResponse response = new UserProfileResponse();
        BeanUtils.copyProperties(user, response);
        
        // 获取技能列表
        List<String> skills = skillRepository.findByUserId(userId)
                .stream()
                .map(Skill::getName)
                .collect(Collectors.toList());
        response.setSkills(skills);
        
        return response;
    }
    
    @Override
    @Transactional
    public void updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        
        // 更新基本信息
        user.setNickname(request.getNickname());
        user.setAvatar(request.getAvatar());
        user.setCollege(request.getCollege());
        user.setMajor(request.getMajor());
        user.setGrade(request.getGrade());
        user.setWechat(request.getWechat());
        user.setQq(request.getQq());
        user.setAllowInvite(request.getAllowInvite());
        
        userRepository.save(user);
        
        // 更新技能标签（增量更新）
        if (request.getSkills() != null) {
            // 获取现有技能
            List<Skill> existingSkills = skillRepository.findByUserId(userId);
            Set<String> existingSkillNames = existingSkills.stream()
                    .map(Skill::getName)
                    .collect(Collectors.toSet());
            
            // 新技能列表（最多5个）
            List<String> newSkillNames = request.getSkills().stream()
                    .limit(5)
                    .collect(Collectors.toList());
            Set<String> newSkillSet = new HashSet<>(newSkillNames);
            
            // 删除不再需要的技能
            List<Skill> skillsToDelete = existingSkills.stream()
                    .filter(skill -> !newSkillSet.contains(skill.getName()))
                    .collect(Collectors.toList());
            if (!skillsToDelete.isEmpty()) {
                skillRepository.deleteAll(skillsToDelete);
            }
            
            // 添加新增的技能
            for (String skillName : newSkillNames) {
                if (!existingSkillNames.contains(skillName)) {
                    Skill skill = new Skill();
                    skill.setUserId(userId);
                    skill.setName(skillName);
                    skillRepository.save(skill);
                }
            }
        }
        
        log.info("用户 {} 更新个人信息成功", userId);
    }
    
    @Override
    public List<UserSearchResponse> searchUsers(UserSearchRequest request) {
        // 获取所有允许邀约的用户
        List<User> allUsers = userRepository.findAll().stream()
                .filter(user -> user.getAllowInvite() != null && user.getAllowInvite())
                .filter(user -> "NORMAL".equals(user.getStatus()))
                .collect(Collectors.toList());
        
        // 应用筛选条件
        List<User> filteredUsers = allUsers.stream()
                .filter(user -> {
                    // 专业筛选（模糊匹配，不区分大小写）
                    if (request.getMajor() != null && !request.getMajor().isEmpty()) {
                        if (user.getMajor() == null || 
                            !user.getMajor().toLowerCase().contains(request.getMajor().toLowerCase())) {
                            return false;
                        }
                    }
                    
                    // 年级筛选（精确匹配）
                    if (request.getGrade() != null && !request.getGrade().isEmpty()) {
                        if (user.getGrade() == null || !user.getGrade().equals(request.getGrade())) {
                            return false;
                        }
                    }
                    
                    return true;
                })
                .collect(Collectors.toList());
        
        // 技能筛选（不区分大小写）
        if (request.getSkills() != null && !request.getSkills().isEmpty()) {
            String[] requiredSkills = request.getSkills().split(",");
            filteredUsers = filteredUsers.stream()
                    .filter(user -> {
                        List<String> userSkills = skillRepository.findByUserId(user.getId())
                                .stream()
                                .map(skill -> skill.getName().toLowerCase())
                                .collect(Collectors.toList());
                        
                        // 至少匹配一个技能（不区分大小写）
                        for (String requiredSkill : requiredSkills) {
                            String normalizedSkill = requiredSkill.trim().toLowerCase();
                            if (userSkills.stream().anyMatch(s -> s.contains(normalizedSkill))) {
                                return true;
                            }
                        }
                        return false;
                    })
                    .collect(Collectors.toList());
        }
        
        // 转换为响应对象
        return filteredUsers.stream()
                .map(user -> {
                    UserSearchResponse response = new UserSearchResponse();
                    response.setId(user.getId());
                    response.setNickname(user.getNickname());
                    response.setAvatar(user.getAvatar());
                    response.setCollege(user.getCollege());
                    response.setMajor(user.getMajor());
                    response.setGrade(user.getGrade());
                    response.setAllowInvite(user.getAllowInvite());
                    
                    // 获取技能标签
                    String skills = skillRepository.findByUserId(user.getId())
                            .stream()
                            .map(Skill::getName)
                            .collect(Collectors.joining(","));
                    response.setSkills(skills);
                    
                    return response;
                })
                .collect(Collectors.toList());
    }
}
