package com.campus.team.dto.response;

import lombok.Data;

@Data
public class UserInfoResponse {
    private Long id;
    private String email;
    private String nickname;
    private String avatar;
    private String college;
    private String major;
    private String grade;
    private String role;
    private Boolean allowInvite;
}
