package com.campus.team.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TeamResponse {
    
    private Long id;
    private Long competitionId;
    private String competitionName;
    private Long leaderId;
    private String leaderName;
    private String leaderAvatar;
    private String title;
    private String description;
    private Integer targetSize;
    private Integer currentSize;
    private String requiredSkills;
    private String questions;
    private String status;
    private String type; // COMPETITION, PROJECT
    private String projectName;
    private String projectType;
    private String projectDuration;
    private Long pendingApplicationCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
