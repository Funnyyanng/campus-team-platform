package com.campus.team.dto.response;

import lombok.Data;

@Data
public class UserSearchResponse {
    
    private Long id;
    private String nickname;
    private String avatar;
    private String college;
    private String major;
    private String grade;
    private String skills;
    private Boolean allowInvite;
}
