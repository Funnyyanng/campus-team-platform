package com.campus.team.service;

import com.campus.team.dto.request.TeamCreateRequest;
import com.campus.team.dto.request.TeamUpdateRequest;
import com.campus.team.dto.response.RefreshCheckResponse;
import com.campus.team.dto.response.TeamResponse;

import java.util.List;

public interface ITeamService {
    
    void createTeam(Long userId, TeamCreateRequest request);
    
    List<TeamResponse> getAllRecruitingTeams();
    
    List<TeamResponse> getTeamsByCompetition(Long competitionId);
    
    TeamResponse getTeamById(Long id);
    
    List<TeamResponse> getMyTeams(Long userId);
    
    void closeRecruitment(Long userId, Long teamId);
    
    void removeMember(Long userId, Long teamId, Long memberId);
    
    void deleteTeam(Long userId, Long teamId);
    
    void transferLeadership(Long userId, Long teamId, Long newLeaderId);
    
    void quitTeam(Long userId, Long teamId, String reason);
    
    // 课题模式方法
    List<TeamResponse> getProjectTeams();
    
    List<TeamResponse> getRecruitingProjectTeams();
    
    // 招募调整方法
    RefreshCheckResponse checkCanRefresh(Long userId, Long teamId);
    
    void refreshTeam(Long userId, Long teamId, TeamUpdateRequest request);
}
