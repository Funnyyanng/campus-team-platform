package com.campus.team.controller;

import com.campus.team.common.Result;
import com.campus.team.dto.request.UpdateProfileRequest;
import com.campus.team.dto.request.UserSearchRequest;
import com.campus.team.dto.response.UserProfileResponse;
import com.campus.team.dto.response.UserSearchResponse;
import com.campus.team.service.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "用户模块")
@RestController
@RequestMapping("/api/user")
public class UserController {
    
    @Autowired
    private IUserService userService;
    
    @Operation(summary = "获取用户信息")
    @GetMapping("/profile")
    public Result<UserProfileResponse> getProfile(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        UserProfileResponse profile = userService.getProfile(userId);
        return Result.success(profile);
    }
    
    @Operation(summary = "更新用户信息")
    @PutMapping("/profile")
    public Result<Void> updateProfile(@Valid @RequestBody UpdateProfileRequest updateRequest,
                                      HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        userService.updateProfile(userId, updateRequest);
        return Result.success();
    }
    
    @Operation(summary = "搜索用户（人才库）")
    @GetMapping("/search")
    public Result<List<UserSearchResponse>> searchUsers(@RequestParam(required = false) String major,
                                                        @RequestParam(required = false) String grade,
                                                        @RequestParam(required = false) String skills) {
        UserSearchRequest searchRequest = new UserSearchRequest();
        searchRequest.setMajor(major);
        searchRequest.setGrade(grade);
        searchRequest.setSkills(skills);
        
        List<UserSearchResponse> users = userService.searchUsers(searchRequest);
        return Result.success(users);
    }
}
