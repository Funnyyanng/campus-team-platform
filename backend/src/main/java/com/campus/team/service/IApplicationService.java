package com.campus.team.service;

import com.campus.team.dto.request.ApplicationRequest;
import com.campus.team.dto.response.ApplicationResponse;

import java.util.List;

public interface IApplicationService {
    
    void applyToTeam(Long userId, ApplicationRequest request);
    
    List<ApplicationResponse> getMyApplications(Long userId);
    
    List<ApplicationResponse> getTeamApplications(Long userId, Long teamId);
    
    List<ApplicationResponse> getTeamMembers(Long userId, Long teamId);
    
    void cancelApplication(Long userId, Long applicationId);
    
    void approveApplication(Long userId, Long applicationId);
    
    void rejectApplication(Long userId, Long applicationId, String reason);
    
    void batchRejectApplications(Long userId, Long teamId, String reason);
}
