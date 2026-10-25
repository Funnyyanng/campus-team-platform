package com.campus.team.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserManageResponse {
    private Long id;
    private String email;
    private String nickname;
    private String avatar;
    private String college;
    private String major;
    private String grade;
    private String role;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
