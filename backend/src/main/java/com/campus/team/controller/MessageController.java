package com.campus.team.controller;

import com.campus.team.common.Result;
import com.campus.team.dto.response.MessageResponse;
import com.campus.team.service.IMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "消息模块")
@RestController
@RequestMapping("/api/messages")
public class MessageController {
    
    @Autowired
    private IMessageService messageService;
    
    @Operation(summary = "获取我的所有消息")
    @GetMapping
    public Result<List<MessageResponse>> getMyMessages(
            HttpServletRequest request,
            @RequestParam(required = false) String type) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        
        List<MessageResponse> messages;
        if (type != null && !type.isEmpty()) {
            messages = messageService.getMessagesByType(userId, type);
        } else {
            messages = messageService.getMyMessages(userId);
        }
        
        return Result.success(messages);
    }
    
    @Operation(summary = "获取未读消息数量")
    @GetMapping("/unread-count")
    public Result<Map<String, Long>> getUnreadCount(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        
        long count = messageService.getUnreadCount(userId);
        Map<String, Long> result = new HashMap<>();
        result.put("count", count);
        return Result.success(result);
    }
    
    @Operation(summary = "标记消息为已读")
    @PutMapping("/{id}/read")
    public Result<Void> markAsRead(
            HttpServletRequest request,
            @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        
        messageService.markAsRead(userId, id);
        return Result.success();
    }
    
    @Operation(summary = "标记所有消息为已读")
    @PutMapping("/read-all")
    public Result<Void> markAllAsRead(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        
        messageService.markAllAsRead(userId);
        return Result.success();
    }
    
    @Operation(summary = "删除消息")
    @DeleteMapping("/{id}")
    public Result<Void> deleteMessage(
            HttpServletRequest request,
            @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        
        messageService.deleteMessage(userId, id);
        return Result.success();
    }
    
    @Operation(summary = "接受邀请")
    @PostMapping("/{id}/accept")
    public Result<Void> acceptInvite(
            HttpServletRequest request,
            @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        
        messageService.acceptInvite(userId, id);
        return Result.success();
    }
    
    @Operation(summary = "拒绝邀请")
    @PostMapping("/{id}/reject")
    public Result<Void> rejectInvite(
            HttpServletRequest request,
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        
        String reason = body != null ? body.get("reason") : null;
        messageService.rejectInvite(userId, id, reason);
        return Result.success();
    }
}
