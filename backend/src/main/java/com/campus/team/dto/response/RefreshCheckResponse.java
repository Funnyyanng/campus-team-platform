package com.campus.team.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 招募调整检查响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefreshCheckResponse {
    
    /**
     * 是否可以调整
     */
    private Boolean canRefresh;
    
    /**
     * 原因说明
     */
    private String reason;
    
    /**
     * 下次可调整时间
     */
    private LocalDateTime nextRefreshTime;
    
    /**
     * 调整期限（创建后3天）
     */
    private LocalDateTime refreshDeadline;
}
