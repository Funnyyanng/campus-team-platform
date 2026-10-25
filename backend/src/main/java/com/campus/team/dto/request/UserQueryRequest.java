package com.campus.team.dto.request;

import lombok.Data;

@Data
public class UserQueryRequest {
    private String keyword;  // 搜索关键词（邮箱、昵称）
    private String status;   // 状态筛选
    private String role;     // 角色筛选
    private Integer page = 1;
    private Integer pageSize = 10;
}
