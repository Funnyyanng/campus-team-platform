package com.campus.team.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CompetitionRequest {
    
    @NotBlank(message = "竞赛名称不能为空")
    private String name;
    
    @NotBlank(message = "竞赛级别不能为空")
    private String level;
    
    private String category;
    
    @NotBlank(message = "主办方不能为空")
    private String organizer;
    
    private String officialUrl;
    
    private String description;
    
    private List<String> keywords;
    
    @NotNull(message = "最小队伍人数不能为空")
    @Min(value = 1, message = "最小队伍人数至少为1")
    private Integer minTeamSize;
    
    @NotNull(message = "最大队伍人数不能为空")
    @Min(value = 1, message = "最大队伍人数至少为1")
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
}
