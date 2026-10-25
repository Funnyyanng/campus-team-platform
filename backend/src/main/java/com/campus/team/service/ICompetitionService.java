package com.campus.team.service;

import com.campus.team.common.PageResult;
import com.campus.team.dto.request.CompetitionQueryRequest;
import com.campus.team.dto.request.CompetitionRequest;
import com.campus.team.dto.response.CompetitionResponse;

import java.util.List;

public interface ICompetitionService {
    
    PageResult<CompetitionResponse> getCompetitionList(CompetitionQueryRequest queryRequest);
    
    List<CompetitionResponse> getActiveCompetitions();
    
    CompetitionResponse getCompetitionById(Long id);
    
    void createCompetition(Long adminId, CompetitionRequest request);
    
    void updateCompetition(Long adminId, Long id, CompetitionRequest request);
    
    void deleteCompetition(Long adminId, Long id);
}
