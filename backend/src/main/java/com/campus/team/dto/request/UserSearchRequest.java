package com.campus.team.dto.request;

import lombok.Data;

@Data
public class UserSearchRequest {
    
    private String major;  // 专业（模糊匹配，不区分大小写）
    private String grade;  // 年级（精确匹配）
    private String skills; // 技能标签（不区分大小写，逗号分隔）
}
