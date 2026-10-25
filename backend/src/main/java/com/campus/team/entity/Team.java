package com.campus.team.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "teams")
public class Team {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column
    private Long competitionId; // 课题模式时为 NULL
    
    @Column(nullable = false)
    private Long leaderId;
    
    @Column(nullable = false, length = 100)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(nullable = false)
    private Integer targetSize;
    
    @Column(nullable = false)
    private Integer currentSize = 1;
    
    @Column(length = 500)
    private String requiredSkills;
    
    @Column(columnDefinition = "TEXT")
    private String questions;
    
    @Column(nullable = false, length = 20)
    private String status = "RECRUITING"; // RECRUITING, FULL, CLOSED
    
    @Column(nullable = false, length = 20)
    private String type = "COMPETITION"; // COMPETITION, PROJECT
    
    @Column(length = 200)
    private String projectName;
    
    @Column(length = 50)
    private String projectType;
    
    @Column(length = 100)
    private String projectDuration;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createTime;
    
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updateTime;
    
    @Column
    private LocalDateTime lastRefreshTime;
}
