package com.campus.team.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ApplicationRequest {
    
    @NotNull(message = "队伍ID不能为空")
    private Long teamId;
    
    private String answers;
    
    private String message;
}
