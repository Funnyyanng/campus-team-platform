package com.campus.team.controller;

import com.campus.team.common.Result;
import com.campus.team.dto.request.InviteRequest;
import com.campus.team.dto.request.TeamCreateRequest;
import com.campus.team.dto.response.TeamResponse;
import com.campus.team.service.IMessageService;
import com.campus.team.service.ITeamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "队伍模块")
@RestController
@RequestMapping("/api/teams")
public class TeamController {
    
    @Autowired
    private ITeamService teamService;
    
    @Autowired
    private IMessageService messageService;
    
    @Operation(summary = "创建队伍")
    @PostMapping
    public Result<Void> createTeam(@Valid @RequestBody TeamCreateRequest request,
                                    HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        teamService.createTeam(userId, request);
        return Result.success();
    }
    
    @Operation(summary = "获取所有招募中的队伍")
    @GetMapping("/recruiting")
    public Result<List<TeamResponse>> getAllRecruitingTeams() {
        List<TeamResponse> teams = teamService.getAllRecruitingTeams();
        return Result.success(teams);
    }
    
    @Operation(summary = "获取我的队伍")
    @GetMapping("/my")
    public Result<List<TeamResponse>> getMyTeams(HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        List<TeamResponse> teams = teamService.getMyTeams(userId);
        return Result.success(teams);
    }
    
    @Operation(summary = "获取竞赛的招募队伍")
    @GetMapping("/competition/{competitionId}")
    public Result<List<TeamResponse>> getTeamsByCompetition(@PathVariable Long competitionId) {
        List<TeamResponse> teams = teamService.getTeamsByCompetition(competitionId);
        return Result.success(teams);
    }
    
    @Operation(summary = "获取队伍详情")
    @GetMapping("/{id}")
    public Result<TeamResponse> getTeamById(@PathVariable Long id) {
        TeamResponse team = teamService.getTeamById(id);
        return Result.success(team);
    }
    
    @Operation(summary = "关闭招募")
    @PostMapping("/{id}/close")
    public Result<Void> closeRecruitment(@PathVariable Long id,
                                          HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        teamService.closeRecruitment(userId, id);
        return Result.success();
    }
    
    @Operation(summary = "移除成员")
    @DeleteMapping("/{id}/members/{memberId}")
    public Result<Void> removeMember(@PathVariable Long id,
                                      @PathVariable Long memberId,
                                      HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        teamService.removeMember(userId, id, memberId);
        return Result.success();
    }
    
    @Operation(summary = "删除队伍")
    @DeleteMapping("/{id}")
    public Result<Void> deleteTeam(@PathVariable Long id,
                                    HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        teamService.deleteTeam(userId, id);
        return Result.success();
    }
    
    @Operation(summary = "转让队长权限")
    @PostMapping("/{id}/transfer")
    public Result<Void> transferLeadership(@PathVariable Long id,
                                           @RequestParam Long newLeaderId,
                                           HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        teamService.transferLeadership(userId, id, newLeaderId);
        return Result.success();
    }
    
    @Operation(summary = "退出队伍")
    @PostMapping("/{id}/quit")
    public Result<Void> quitTeam(@PathVariable Long id,
                                  @RequestParam String reason,
                                  HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        teamService.quitTeam(userId, id, reason);
        return Result.success();
    }
    
    @Operation(summary = "获取所有课题队伍")
    @GetMapping("/projects")
    public Result<List<TeamResponse>> getProjectTeams() {
        List<TeamResponse> teams = teamService.getProjectTeams();
        return Result.success(teams);
    }
    
    @Operation(summary = "获取招募中的课题队伍")
    @GetMapping("/projects/recruiting")
    public Result<List<TeamResponse>> getRecruitingProjectTeams() {
        List<TeamResponse> teams = teamService.getRecruitingProjectTeams();
        return Result.success(teams);
    }
    
    @Operation(summary = "检查是否可以调整招募")
    @GetMapping("/{id}/can-refresh")
    public Result<com.campus.team.dto.response.RefreshCheckResponse> checkCanRefresh(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        com.campus.team.dto.response.RefreshCheckResponse response = teamService.checkCanRefresh(userId, id);
        return Result.success(response);
    }
    
    @Operation(summary = "调整招募信息")
    @PutMapping("/{id}/refresh")
    public Result<Void> refreshTeam(@PathVariable Long id,
                                     @Valid @RequestBody com.campus.team.dto.request.TeamUpdateRequest request,
                                     HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        teamService.refreshTeam(userId, id, request);
        return Result.success();
    }
    
    @Operation(summary = "邀请用户加入队伍")
    @PostMapping("/{id}/invite")
    public Result<Void> inviteUser(@PathVariable Long id,
                                    @Valid @RequestBody InviteRequest request,
                                    HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        messageService.sendInviteMessage(userId, request.getInviteeId(), id, request.getMessage());
        return Result.success();
    }
}
