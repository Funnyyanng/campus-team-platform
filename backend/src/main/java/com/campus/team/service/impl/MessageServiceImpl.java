package com.campus.team.service.impl;

import com.campus.team.dto.response.MessageResponse;
import com.campus.team.entity.Message;
import com.campus.team.entity.Team;
import com.campus.team.entity.TeamApplication;
import com.campus.team.entity.User;
import com.campus.team.exception.BusinessException;
import com.campus.team.repository.*;
import com.campus.team.service.IMessageService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MessageServiceImpl implements IMessageService {
    
    @Autowired
    private MessageRepository messageRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private TeamRepository teamRepository;
    
    @Autowired
    private TeamApplicationRepository applicationRepository;
    
    @Autowired
    private CompetitionRepository competitionRepository;
    
    @Override
    public List<MessageResponse> getMyMessages(Long userId) {
        List<Message> messages = messageRepository.findByReceiverIdOrderByCreateTimeDesc(userId);
        return messages.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<MessageResponse> getMessagesByType(Long userId, String type) {
        List<Message> messages = messageRepository.findByReceiverIdAndTypeOrderByCreateTimeDesc(userId, type);
        return messages.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public long getUnreadCount(Long userId) {
        return messageRepository.countByReceiverIdAndIsRead(userId, false);
    }
    
    @Override
    @Transactional
    public void markAsRead(Long userId, Long messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new BusinessException("消息不存在"));
        
        if (!message.getReceiverId().equals(userId)) {
            throw new BusinessException("无权操作此消息");
        }
        
        message.setIsRead(true);
        messageRepository.save(message);
    }
    
    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        List<Message> unreadMessages = messageRepository.findByReceiverIdAndIsReadOrderByCreateTimeDesc(userId, false);
        unreadMessages.forEach(message -> message.setIsRead(true));
        messageRepository.saveAll(unreadMessages);
    }
    
    @Override
    @Transactional
    public void deleteMessage(Long userId, Long messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new BusinessException("消息不存在"));
        
        if (!message.getReceiverId().equals(userId)) {
            throw new BusinessException("无权删除此消息");
        }
        
        messageRepository.delete(message);
    }
    
    @Override
    @Transactional
    public void sendSystemMessage(Long receiverId, String title, String content, String type, Long relatedId) {
        Message message = new Message();
        message.setType(type);
        message.setTitle(title);
        message.setContent(content);
        message.setSenderId(null); // 系统消息
        message.setReceiverId(receiverId);
        message.setRelatedId(relatedId);
        message.setIsRead(false);
        
        messageRepository.save(message);
    }
    
    @Override
    @Transactional
    public void sendInviteMessage(Long senderId, Long receiverId, Long teamId, String inviteMessage) {
        // 验证发送者
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new BusinessException("发送者不存在"));
        
        // 验证接收者
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new BusinessException("接收者不存在"));
        
        // 验证接收者是否允许邀约
        if (!receiver.getAllowInvite()) {
            throw new BusinessException("该用户不接受邀约");
        }
        
        // 验证队伍
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new BusinessException("队伍不存在"));
        
        // 验证是否是队长
        if (!team.getLeaderId().equals(senderId)) {
            throw new BusinessException("只有队长可以邀请成员");
        }
        
        // 验证队伍状态
        if (!"RECRUITING".equals(team.getStatus())) {
            throw new BusinessException("该队伍未在招募中");
        }
        
        // 验证队伍是否已满
        if (team.getCurrentSize() >= team.getTargetSize()) {
            throw new BusinessException("队伍已满员");
        }
        
        // 验证用户是否已在队伍中
        boolean isLeader = team.getLeaderId().equals(receiverId);
        boolean isMember = applicationRepository.existsByTeamIdAndApplicantIdAndStatus(teamId, receiverId, "APPROVED");
        if (isLeader || isMember) {
            throw new BusinessException("该用户已在队伍中");
        }
        
        // 验证是否已发送过邀请
        boolean alreadyInvited = messageRepository.existsBySenderIdAndReceiverIdAndRelatedIdAndType(
                senderId, receiverId, teamId, "INVITE");
        if (alreadyInvited) {
            throw new BusinessException("您已经邀请过该用户");
        }
        
        // 创建邀请消息
        Message message = new Message();
        message.setType("INVITE");
        message.setTitle("队伍邀请");
        message.setContent(String.format("%s 邀请你加入队伍「%s」\n\n留言：%s", 
                sender.getNickname(), 
                team.getTitle(), 
                inviteMessage != null ? inviteMessage : "期待你的加入！"));
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setRelatedId(teamId);
        message.setIsRead(false);
        message.setInviteStatus("PENDING");
        
        messageRepository.save(message);
    }
    
    @Override
    @Transactional
    public void acceptInvite(Long userId, Long messageId) {
        // 查找消息
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new BusinessException("消息不存在"));
        
        // 验证是否是接收者
        if (!message.getReceiverId().equals(userId)) {
            throw new BusinessException("无权操作此消息");
        }
        
        // 验证消息类型
        if (!"INVITE".equals(message.getType())) {
            throw new BusinessException("该消息不是邀请");
        }
        
        // 验证邀请状态
        if (!"PENDING".equals(message.getInviteStatus())) {
            throw new BusinessException("该邀请已被处理");
        }
        
        // 查找队伍
        Team team = teamRepository.findById(message.getRelatedId())
                .orElseThrow(() -> new BusinessException("队伍不存在"));
        
        // 验证队伍状态
        if (!"RECRUITING".equals(team.getStatus())) {
            throw new BusinessException("该队伍未在招募中");
        }
        
        // 验证队伍是否已满
        if (team.getCurrentSize() >= team.getTargetSize()) {
            throw new BusinessException("队伍已满员");
        }
        
        // 验证用户是否已在队伍中
        boolean isLeader = team.getLeaderId().equals(userId);
        boolean isMember = applicationRepository.existsByTeamIdAndApplicantIdAndStatus(
                team.getId(), userId, "APPROVED");
        if (isLeader || isMember) {
            throw new BusinessException("您已在队伍中");
        }
        
        // 创建申请记录（状态为APPROVED）
        TeamApplication application = new TeamApplication();
        application.setTeamId(team.getId());
        application.setApplicantId(userId);
        application.setStatus("APPROVED");
        application.setMessage("通过邀请加入");
        applicationRepository.save(application);
        
        // 更新队伍人数
        team.setCurrentSize(team.getCurrentSize() + 1);
        if (team.getCurrentSize() >= team.getTargetSize()) {
            team.setStatus("FULL");
        }
        teamRepository.save(team);
        
        // 更新邀请状态
        message.setInviteStatus("ACCEPTED");
        message.setIsRead(true);
        messageRepository.save(message);
        
        // 取消该用户的其他待处理邀请
        List<Message> otherInvites = messageRepository.findByReceiverIdAndTypeOrderByCreateTimeDesc(userId, "INVITE")
                .stream()
                .filter(m -> "PENDING".equals(m.getInviteStatus()) && !m.getId().equals(messageId))
                .collect(Collectors.toList());
        otherInvites.forEach(m -> m.setInviteStatus("EXPIRED"));
        messageRepository.saveAll(otherInvites);
        
        // 发送通知给队长
        sendSystemMessage(team.getLeaderId(), 
                "邀请已接受", 
                String.format("%s 接受了你的邀请，已加入队伍", 
                        userRepository.findById(userId).get().getNickname()),
                "TEAM",
                team.getId());
    }
    
    @Override
    @Transactional
    public void rejectInvite(Long userId, Long messageId, String reason) {
        // 查找消息
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new BusinessException("消息不存在"));
        
        // 验证是否是接收者
        if (!message.getReceiverId().equals(userId)) {
            throw new BusinessException("无权操作此消息");
        }
        
        // 验证消息类型
        if (!"INVITE".equals(message.getType())) {
            throw new BusinessException("该消息不是邀请");
        }
        
        // 验证邀请状态
        if (!"PENDING".equals(message.getInviteStatus())) {
            throw new BusinessException("该邀请已被处理");
        }
        
        // 更新邀请状态
        message.setInviteStatus("REJECTED");
        message.setIsRead(true);
        messageRepository.save(message);
        
        // 发送通知给队长（可选）
        Team team = teamRepository.findById(message.getRelatedId()).orElse(null);
        if (team != null) {
            sendSystemMessage(team.getLeaderId(), 
                    "邀请被拒绝", 
                    String.format("%s 拒绝了你的邀请", 
                            userRepository.findById(userId).get().getNickname()),
                    "TEAM",
                    team.getId());
        }
    }
    
    private MessageResponse convertToResponse(Message message) {
        MessageResponse response = new MessageResponse();
        BeanUtils.copyProperties(message, response);
        
        // 获取发送者信息
        if (message.getSenderId() != null) {
            userRepository.findById(message.getSenderId()).ifPresent(sender -> {
                response.setSenderName(sender.getNickname());
                response.setSenderAvatar(sender.getAvatar());
            });
        }
        
        // 获取关联队伍信息
        if (message.getRelatedId() != null && 
            ("INVITE".equals(message.getType()) || "TEAM".equals(message.getType()))) {
            teamRepository.findById(message.getRelatedId()).ifPresent(team -> {
                response.setTeamTitle(team.getTitle());
                
                if ("COMPETITION".equals(team.getType()) && team.getCompetitionId() != null) {
                    competitionRepository.findById(team.getCompetitionId()).ifPresent(competition -> {
                        response.setCompetitionName(competition.getName());
                    });
                }
            });
        }
        
        return response;
    }
}
