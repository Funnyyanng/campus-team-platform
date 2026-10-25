package com.campus.team.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ApplicationResponse {
    
    private Long id;
    private Long teamId;
    private String teamTitle;
    private String competitionName;
    private Long applicantId;
    private String applicantName;
    private String applicantAvatar;
    private String applicantCollege;
    private String applicantMajor;
    private String applicantGrade;
    private String applicantSkills;
    private String applicantWechat;
    private String applicantQq;
    private String answers;
    private String message;
    private String status;
    private String rejectReason;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
