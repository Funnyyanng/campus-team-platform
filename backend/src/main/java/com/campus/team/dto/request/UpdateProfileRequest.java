package com.campus.team.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class UpdateProfileRequest {
    
    @NotBlank(message = "昵称不能为空")
    private String nickname;
    
    private String avatar;
    
    @NotBlank(message = "学院不能为空")
    private String college;
    
    @NotBlank(message = "专业不能为空")
    private String major;
    
    @NotBlank(message = "年级不能为空")
    private String grade;
    
    private String wechat;
    
    private String qq;
    
    private List<String> skills;
    
    private Boolean allowInvite;
}
