package com.campus.team.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "competition")
public class Competition {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 200)
    private String name;
    
    @Column(length = 50)
    private String level;  // 国家级、省级、校级
    
    @Column(length = 100)
    private String category;  // 竞赛类别：学科竞赛、创新创业、文体竞赛等
    
    @Column(length = 200)
    private String organizer;  // 主办方
    
    @Column(name = "official_url", length = 500)
    private String officialUrl;  // 官网链接
    
    @Column(columnDefinition = "TEXT")
    private String description;  // 竞赛描述
    
    @Column(columnDefinition = "TEXT")
    private String keywords;  // 关键词（JSON数组）
    
    @Column(name = "min_team_size")
    private Integer minTeamSize;  // 最小队伍人数
    
    @Column(name = "max_team_size")
    private Integer maxTeamSize;  // 最大队伍人数
    
    @Column(name = "registration_start")
    private LocalDateTime registrationStart;  // 报名开始时间
    
    @Column(name = "registration_end")
    private LocalDateTime registrationEnd;  // 报名截止时间
    
    @Column(name = "competition_start")
    private LocalDateTime competitionStart;  // 竞赛开始时间
    
    @Column(name = "competition_end")
    private LocalDateTime competitionEnd;  // 竞赛结束时间
    
    @Column
    private LocalDateTime deadline;  // 作品提交截止时间
    
    @Column(length = 100)
    private String prize;  // 奖项设置（如：一等奖、二等奖）
    
    @Column(name = "participation_fee")
    private Integer participationFee;  // 参赛费用（单位：元，0表示免费）
    
    @Column(columnDefinition = "TEXT")
    private String requirements;  // 参赛要求
    
    @Column(name = "contact_info", length = 200)
    private String contactInfo;  // 联系方式
    
    @Column(length = 20)
    private String status = "ACTIVE";  // ACTIVE, ENDED, DRAFT
    
    @CreationTimestamp
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;
    
    @UpdateTimestamp
    @Column(name = "update_time")
    private LocalDateTime updateTime;
}
