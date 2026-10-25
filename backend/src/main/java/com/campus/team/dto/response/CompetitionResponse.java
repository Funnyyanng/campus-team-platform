package com.campus.team.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CompetitionResponse {
    private Long id;
    private String name;
    private String level;
    private String category;
    private String organizer;
    private String officialUrl;
    private String description;
    private List<String> keywords;
    private Integer minTeamSize;
    private Integer maxTeamSize;
    private LocalDateTime registrationStart;
    private LocalDateTime registrationEnd;
    private LocalDateTime competitionStart;
    private LocalDateTime competitionEnd;
    private LocalDateTime deadline;
    private String prize;
    private Integer participationFee;
    private String requirements;
    private String contactInfo;
    private String status;
    private Long recruitingTeamCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
