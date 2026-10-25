package com.campus.team.controller;

import com.campus.team.common.Result;
import com.campus.team.dto.request.ApplicationRequest;
import com.campus.team.dto.response.ApplicationResponse;
import com.campus.team.service.IApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "申请模块")
@RestController
@RequestMapping("/api/applications")
public class ApplicationController {
    
    @Autowired
    private IApplicationService applicationService;
    
    @Operation(summary = "申请加入队伍")
    @PostMapping
    public Result<Void> applyToTeam(@Valid @RequestBody ApplicationRequest request,
                                     HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        applicationService.applyToTeam(userId, request);
        return Result.success();
    }
    
    @Operation(summary = "获取我的申请")
    @GetMapping("/my")
    public Result<List<ApplicationResponse>> getMyApplications(HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        List<ApplicationResponse> applications = applicationService.getMyApplications(userId);
        return Result.success(applications);
    }
    
    @Operation(summary = "获取队伍的申请列表")
    @GetMapping("/team/{teamId}")
    public Result<List<ApplicationResponse>> getTeamApplications(@PathVariable Long teamId,
                                                                  HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        List<ApplicationResponse> applications = applicationService.getTeamApplications(userId, teamId);
        return Result.success(applications);
    }
    
    @Operation(summary = "撤销申请")
    @DeleteMapping("/{applicationId}")
    public Result<Void> cancelApplication(@PathVariable Long applicationId,
                                          HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        applicationService.cancelApplication(userId, applicationId);
        return Result.success();
    }
    
    @Operation(summary = "通过申请")
    @PostMapping("/{applicationId}/approve")
    public Result<Void> approveApplication(@PathVariable Long applicationId,
                                           HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        applicationService.approveApplication(userId, applicationId);
        return Result.success();
    }
    
    @Operation(summary = "拒绝申请")
    @PostMapping("/{applicationId}/reject")
    public Result<Void> rejectApplication(@PathVariable Long applicationId,
                                          @RequestParam String reason,
                                          HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        applicationService.rejectApplication(userId, applicationId, reason);
        return Result.success();
    }
    
    @Operation(summary = "批量拒绝申请（一键忽略）")
    @PostMapping("/team/{teamId}/batch-reject")
    public Result<Void> batchRejectApplications(@PathVariable Long teamId,
                                                @RequestParam String reason,
                                                HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        applicationService.batchRejectApplications(userId, teamId, reason);
        return Result.success();
    }
    
    @Operation(summary = "获取队伍成员列表")
    @GetMapping("/team/{teamId}/members")
    public Result<List<ApplicationResponse>> getTeamMembers(@PathVariable Long teamId,
                                                             HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        List<ApplicationResponse> members = applicationService.getTeamMembers(userId, teamId);
        return Result.success(members);
    }
}
