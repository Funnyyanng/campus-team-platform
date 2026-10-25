package com.campus.team.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BanUserRequest {
    
    @NotBlank(message = "状态不能为空")
    private String status;  // NORMAL, BANNED, MUTED
    
    private String reason;  // 封禁原因
}
