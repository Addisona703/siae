package com.hngy.siae.auth.controller;

import com.hngy.siae.auth.common.ApiResult;
import com.hngy.siae.auth.dto.LoginFailResponse;
import com.hngy.siae.auth.dto.LoginLogResponse;
import com.hngy.siae.auth.dto.PageResult;
import com.hngy.siae.auth.service.LogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * 日志控制器
 * 
 * @author KEYKB
 */
@Tag(name = "日志管理", description = "系统日志查询")
@RestController
@RequestMapping("/api/v1/auth/logs")
public class LogController {
    
    private final LogService logService;
    
    /**
     * 构造函数
     *
     * @param logService 日志服务
     */
    public LogController(LogService logService) {
        this.logService = logService;
    }
    
    /**
     * 查询登录日志
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param username  用户名
     * @param status    状态
     * @param page      页码
     * @param size      每页记录数
     * @return 登录日志分页结果
     */
    @Operation(summary = "查询登录日志", description = "分页查询登录日志")
    @GetMapping("/login")
    @PreAuthorize("hasAuthority('system:log:query')")
    public ApiResult<PageResult<LoginLogResponse>> getLoginLogs(
            @Parameter(description = "查询开始时间") 
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @Parameter(description = "查询结束时间") 
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @Parameter(description = "用户名") 
            @RequestParam(required = false) String username,
            @Parameter(description = "登录状态：0失败，1成功") 
            @RequestParam(required = false) Integer status,
            @Parameter(description = "页码") 
            @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页记录数") 
            @RequestParam(defaultValue = "10") int size) {
        
        PageResult<LoginLogResponse> pageResult = logService.getLoginLogs(startTime, endTime, username, status, page, size);
        return ApiResult.success(pageResult);
    }
    
    /**
     * 查询登录失败记录
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param username  用户名
     * @param loginIp   登录IP
     * @param page      页码
     * @param size      每页记录数
     * @return 登录失败日志分页结果
     */
    @Operation(summary = "查询登录失败记录", description = "分页查询登录失败记录")
    @GetMapping("/login-fail")
    @PreAuthorize("hasAuthority('system:log:query')")
    public ApiResult<PageResult<LoginFailResponse>> getLoginFailLogs(
            @Parameter(description = "查询开始时间") 
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @Parameter(description = "查询结束时间") 
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @Parameter(description = "用户名") 
            @RequestParam(required = false) String username,
            @Parameter(description = "登录IP") 
            @RequestParam(required = false) String loginIp,
            @Parameter(description = "页码") 
            @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页记录数") 
            @RequestParam(defaultValue = "10") int size) {
        
        PageResult<LoginFailResponse> pageResult = logService.getLoginFailLogs(startTime, endTime, username, loginIp, page, size);
        return ApiResult.success(pageResult);
    }
} 