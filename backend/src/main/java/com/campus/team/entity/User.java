package com.campus.team.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "user")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 100)
    private String email;
    
    @Column(nullable = false, length = 255)
    private String password;
    
    @Column(length = 50)
    private String nickname;
    
    @Column(length = 255)
    private String avatar;
    
    @Column(length = 100)
    private String college;
    
    @Column(length = 100)
    private String major;
    
    @Column(length = 20)
    private String grade;
    
    @Column(length = 100)
    private String wechat;
    
    @Column(length = 50)
    private String qq;
    
    @Column(name = "allow_invite")
    private Boolean allowInvite = true;
    
    @Column(length = 20)
    private String role = "USER";  // USER, ADMIN
    
    @Column(length = 20)
    private String status = "NORMAL";  // NORMAL, BANNED, MUTED
    
    @CreationTimestamp
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;
    
    @UpdateTimestamp
    @Column(name = "update_time")
    private LocalDateTime updateTime;
}
