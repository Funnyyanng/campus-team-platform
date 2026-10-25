package com.campus.team.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class UserProfileResponse {
    private Long id;
    private String email;
    private String nickname;
    private String avatar;
    private String college;
    private String major;
    private String grade;
    private String wechat;
    private String qq;
    private String role;
    private List<String> skills;
    private Boolean allowInvite;
}
