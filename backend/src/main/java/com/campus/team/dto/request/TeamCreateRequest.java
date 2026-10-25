package com.campus.team.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TeamCreateRequest {
    
    @NotBlank(message = "队伍类型不能为空")
    private String type; // COMPETITION, PROJECT
    
    private Long competitionId; // 竞赛模式时必填
    
    private String projectName; // 课题模式时必填
    
    private String projectType; // 课题模式时必填
    
    private String projectDuration; // 课题模式时选填
    
    @NotBlank(message = "队伍标题不能为空")
    private String title;
    
    private String description;
    
    @NotNull(message = "目标人数不能为空")
    @Min(value = 1, message = "目标人数至少为1人")
    @Max(value = 10, message = "目标人数最多为10人")
    private Integer targetSize;
    
    private String requiredSkills;
    
    private String questions;
}
