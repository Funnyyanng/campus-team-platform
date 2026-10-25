package com.campus.team.service;

import com.campus.team.dto.request.UpdateProfileRequest;
import com.campus.team.dto.request.UserSearchRequest;
import com.campus.team.dto.response.UserProfileResponse;
import com.campus.team.dto.response.UserSearchResponse;

import java.util.List;

public interface IUserService {
    
    UserProfileResponse getProfile(Long userId);
    
    void updateProfile(Long userId, UpdateProfileRequest request);
    
    List<UserSearchResponse> searchUsers(UserSearchRequest request);
}
