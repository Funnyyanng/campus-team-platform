package com.campus.team.controller;

import com.campus.team.common.PageResult;
import com.campus.team.common.Result;
import com.campus.team.dto.request.BanUserRequest;
import com.campus.team.dto.request.UserQueryRequest;
import com.campus.team.dto.response.AnnouncementResponse;
import com.campus.team.dto.response.DashboardChartsResponse;
import com.campus.team.dto.response.DashboardStatsResponse;
import com.campus.team.dto.response.TeamResponse;
import com.campus.team.dto.response.UserManageResponse;
import com.campus.team.service.IAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "管理员模块")
@RestController
@RequestMapping("/api/admin")
public class AdminController {
    
    @Autowired
    private IAdminService adminService;
    
    @Operation(summary = "获取用户列表")
    @GetMapping("/users")
    public Result<PageResult<UserManageResponse>> getUserList(
            @Valid UserQueryRequest queryRequest,
            HttpServletRequest request) {
        Long adminId = (Long) request.getAttribute("userId");
        PageResult<UserManageResponse> result = adminService.getUserList(adminId, queryRequest);
        return Result.success(result);
    }
    
    @Operation(summary = "封禁/解封用户")
    @PutMapping("/user/{userId}/ban")
    public Result<Void> banUser(@PathVariable Long userId,
                                 @Valid @RequestBody BanUserRequest banRequest,
                                 HttpServletRequest request) {
        Long adminId = (Long) request.getAttribute("userId");
        adminService.banUser(adminId, userId, banRequest);
        return Result.success();
    }
    
    @Operation(summary = "删除用户")
    @DeleteMapping("/user/{userId}")
    public Result<Void> deleteUser(@PathVariable Long userId,
                                    HttpServletRequest request) {
        Long adminId = (Long) request.getAttribute("userId");
        adminService.deleteUser(adminId, userId);
        return Result.success();
    }
    
    @Operation(summary = "发布全局公告")
    @PostMapping("/announcement")
    public Result<Void> publishAnnouncement(
            @RequestBody java.util.Map<String, String> request,
            HttpServletRequest httpRequest) {
        Long adminId = (Long) httpRequest.getAttribute("userId");
        String title = request.get("title");
        String content = request.get("content");
        adminService.publishAnnouncement(adminId, title, content);
        return Result.success();
    }
    
    @Operation(summary = "获取公告历史记录")
    @GetMapping("/announcements")
    public Result<List<AnnouncementResponse>> getAnnouncementHistory(HttpServletRequest request) {
        Long adminId = (Long) request.getAttribute("userId");
        List<AnnouncementResponse> announcements = adminService.getAnnouncementHistory(adminId);
        return Result.success(announcements);
    }
    
    @Operation(summary = "获取数据概览统计")
    @GetMapping("/dashboard/stats")
    public Result<DashboardStatsResponse> getDashboardStats(HttpServletRequest request) {
        Long adminId = (Long) request.getAttribute("userId");
        DashboardStatsResponse stats = adminService.getDashboardStats(adminId);
        return Result.success(stats);
    }
    
    @Operation(summary = "获取数据概览图表")
    @GetMapping("/dashboard/charts")
    public Result<DashboardChartsResponse> getDashboardCharts(HttpServletRequest request) {
        Long adminId = (Long) request.getAttribute("userId");
        DashboardChartsResponse charts = adminService.getDashboardCharts(adminId);
        return Result.success(charts);
    }
    
    @Operation(summary = "获取所有队伍（管理后台）")
    @GetMapping("/teams")
    public Result<List<TeamResponse>> getAllTeams(HttpServletRequest request) {
        Long adminId = (Long) request.getAttribute("userId");
        List<TeamResponse> teams = adminService.getAllTeams(adminId);
        return Result.success(teams);
    }
    
    @Operation(summary = "删除队伍（管理后台）")
    @DeleteMapping("/team/{teamId}")
    public Result<Void> deleteTeam(@PathVariable Long teamId,
                                    @RequestBody java.util.Map<String, String> request,
                                    HttpServletRequest httpRequest) {
        Long adminId = (Long) httpRequest.getAttribute("userId");
        String reason = request.get("reason");
        adminService.deleteTeam(adminId, teamId, reason);
        return Result.success();
    }
}

