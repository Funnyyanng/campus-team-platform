package com.campus.team.dto.response;

import lombok.Data;

@Data
public class DashboardStatsResponse {
    
    private Long totalUsers;
    private Long activeTeams;
    private Long activeCompetitions;
}
