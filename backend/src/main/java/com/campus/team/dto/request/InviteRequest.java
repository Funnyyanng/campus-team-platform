package com.campus.team.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InviteRequest {
    
    @NotNull(message = "被邀请人ID不能为空")
    private Long inviteeId;
    
    private String message;
}
