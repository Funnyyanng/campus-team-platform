package com.campus.team.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "messages")
public class Message {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 20)
    private String type; // SYSTEM, INVITE, APPLICATION, TEAM
    
    @Column(nullable = false, length = 200)
    private String title;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
    
    @Column
    private Long senderId; // 发送者ID（系统消息为NULL）
    
    @Column(nullable = false)
    private Long receiverId; // 接收者ID
    
    @Column
    private Long relatedId; // 关联ID（队伍ID、申请ID等）
    
    @Column(nullable = false)
    private Boolean isRead = false;
    
    @Column(length = 20)
    private String inviteStatus; // PENDING, ACCEPTED, REJECTED, EXPIRED
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createTime;
    
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updateTime;
}
