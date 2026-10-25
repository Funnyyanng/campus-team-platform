package com.campus.team.service;

import com.campus.team.common.PageResult;
import com.campus.team.dto.request.BanUserRequest;
import com.campus.team.dto.request.UserQueryRequest;
import com.campus.team.dto.response.AnnouncementResponse;
import com.campus.team.dto.response.TeamResponse;
import com.campus.team.dto.response.UserManageResponse;

import java.util.List;

public interface IAdminService {
    
    PageResult<UserManageResponse> getUserList(Long adminId, UserQueryRequest queryRequest);
    
    void banUser(Long adminId, Long userId, BanUserRequest request);
    
    void deleteUser(Long adminId, Long userId);
    
    void publishAnnouncement(Long adminId, String title, String content);
    
    List<AnnouncementResponse> getAnnouncementHistory(Long adminId);
    
    com.campus.team.dto.response.DashboardStatsResponse getDashboardStats(Long adminId);
    
    com.campus.team.dto.response.DashboardChartsResponse getDashboardCharts(Long adminId);
    
    List<TeamResponse> getAllTeams(Long adminId);
    
    void deleteTeam(Long adminId, Long teamId, String reason);
}
