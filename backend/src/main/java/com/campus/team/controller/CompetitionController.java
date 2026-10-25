package com.campus.team.controller;

import com.campus.team.common.PageResult;
import com.campus.team.common.Result;
import com.campus.team.dto.request.CompetitionQueryRequest;
import com.campus.team.dto.request.CompetitionRequest;
import com.campus.team.dto.response.CompetitionResponse;
import com.campus.team.service.ICompetitionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "竞赛模块")
@RestController
@RequestMapping("/api")
public class CompetitionController {
    
    @Autowired
    private ICompetitionService competitionService;
    
    @Operation(summary = "获取竞赛列表")
    @GetMapping("/competitions")
    public Result<PageResult<CompetitionResponse>> getCompetitionList(
            @Valid CompetitionQueryRequest queryRequest) {
        PageResult<CompetitionResponse> result = competitionService.getCompetitionList(queryRequest);
        return Result.success(result);
    }
    
    @Operation(summary = "获取进行中的竞赛")
    @GetMapping("/competitions/active")
    public Result<List<CompetitionResponse>> getActiveCompetitions() {
        List<CompetitionResponse> result = competitionService.getActiveCompetitions();
        return Result.success(result);
    }
    
    @Operation(summary = "获取竞赛详情")
    @GetMapping("/competition/{id}")
    public Result<CompetitionResponse> getCompetitionById(@PathVariable Long id) {
        CompetitionResponse response = competitionService.getCompetitionById(id);
        return Result.success(response);
    }
    
    @Operation(summary = "创建竞赛")
    @PostMapping("/admin/competition")
    public Result<Void> createCompetition(@Valid @RequestBody CompetitionRequest request,
                                          HttpServletRequest httpRequest) {
        Long adminId = (Long) httpRequest.getAttribute("userId");
        competitionService.createCompetition(adminId, request);
        return Result.success();
    }
    
    @Operation(summary = "更新竞赛")
    @PutMapping("/admin/competition/{id}")
    public Result<Void> updateCompetition(@PathVariable Long id,
                                          @Valid @RequestBody CompetitionRequest request,
                                          HttpServletRequest httpRequest) {
        Long adminId = (Long) httpRequest.getAttribute("userId");
        competitionService.updateCompetition(adminId, id, request);
        return Result.success();
    }
    
    @Operation(summary = "删除竞赛")
    @DeleteMapping("/admin/competition/{id}")
    public Result<Void> deleteCompetition(@PathVariable Long id,
                                          HttpServletRequest httpRequest) {
        Long adminId = (Long) httpRequest.getAttribute("userId");
        competitionService.deleteCompetition(adminId, id);
        return Result.success();
    }
}
