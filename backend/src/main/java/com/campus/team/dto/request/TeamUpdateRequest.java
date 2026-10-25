package com.campus.team.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * 队伍更新请求（用于招募调整）
 */
@Data
public class TeamUpdateRequest {
    
    @NotBlank(message = "队伍标题不能为空")
    @Size(min = 2, max = 100, message = "队伍标题长度必须在2-100个字符之间")
    private String title;
    
    @NotBlank(message = "队伍简介不能为空")
    @Size(min = 10, max = 1000, message = "队伍简介长度必须在10-1000个字符之间")
    private String description;
    
    @NotNull(message = "目标人数不能为空")
    @Min(value = 2, message = "目标人数不能少于2人")
    @Max(value = 10, message = "目标人数不能超过10人")
    private Integer targetSize;
    
    @Size(max = 200, message = "需求技能长度不能超过200个字符")
    private String requiredSkills;
    
    @Size(max = 500, message = "验证问题长度不能超过500个字符")
    private String questions;
}
