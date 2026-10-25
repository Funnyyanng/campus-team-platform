package com.campus.team.service;

import com.campus.team.dto.response.MessageResponse;

import java.util.List;

public interface IMessageService {
    
    /**
     * 获取用户的所有消息
     */
    List<MessageResponse> getMyMessages(Long userId);
    
    /**
     * 根据类型获取消息
     */
    List<MessageResponse> getMessagesByType(Long userId, String type);
    
    /**
     * 获取未读消息数量
     */
    long getUnreadCount(Long userId);
    
    /**
     * 标记消息为已读
     */
    void markAsRead(Long userId, Long messageId);
    
    /**
     * 标记所有消息为已读
     */
    void markAllAsRead(Long userId);
    
    /**
     * 删除消息
     */
    void deleteMessage(Long userId, Long messageId);
    
    /**
     * 发送系统消息
     */
    void sendSystemMessage(Long receiverId, String title, String content, String type, Long relatedId);
    
    /**
     * 发送邀请消息
     */
    void sendInviteMessage(Long senderId, Long receiverId, Long teamId, String message);
    
    /**
     * 接受邀请
     */
    void acceptInvite(Long userId, Long messageId);
    
    /**
     * 拒绝邀请
     */
    void rejectInvite(Long userId, Long messageId, String reason);
}
