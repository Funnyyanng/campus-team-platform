package com.campus.team.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MessageResponse {
    
    private Long id;
    private String type;
    private String title;
    private String content;
    private Long senderId;
    private String senderName;
    private String senderAvatar;
    private Long receiverId;
    private Long relatedId;
    private Boolean isRead;
    private String inviteStatus;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    // 关联信息
    private String teamTitle;
    private String competitionName;
}
