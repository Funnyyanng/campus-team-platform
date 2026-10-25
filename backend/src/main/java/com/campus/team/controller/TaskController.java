package com.campus.team.controller;

import com.campus.team.common.Result;
import com.campus.team.task.AutomationTask;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "定时任务模块")
@RestController
@RequestMapping("/api/admin/tasks")
public class TaskController {
    
    @Autowired
    private AutomationTask automationTask;
    
    @Operation(summary = "手动执行申请超时失效任务")
    @PostMapping("/expire-applications")
    public Result<Void> expireApplications() {
        automationTask.expireApplications();
        return Result.success();
    }
    
    @Operation(summary = "手动执行自动满员任务")
    @PostMapping("/close-full-teams")
    public Result<Void> closeFullTeams() {
        automationTask.autoCloseFullTeams();
        return Result.success();
    }
    
    @Operation(summary = "手动执行竞赛过期任务")
    @PostMapping("/expire-competitions")
    public Result<Void> expireCompetitions() {
        automationTask.expireCompetitions();
        return Result.success();
    }
}
