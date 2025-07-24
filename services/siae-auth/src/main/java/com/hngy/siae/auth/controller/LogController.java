package com.hngy.siae.auth.controller;

import com.hngy.siae.auth.dto.response.LoginFailVO;
import com.hngy.siae.auth.dto.response.LoginLogVO;
import com.hngy.siae.core.result.Result;
import com.hngy.siae.core.dto.PageVO;
import com.hngy.siae.auth.service.LogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

import static com.hngy.siae.core.permissions.AuthPermissions.*;

/**
 * 日志控制器
 * 
 * @author KEYKB
 */
@Tag(name = "日志管理", description = "日志查询相关API")
@RestController
@RequestMapping("/logs")
@RequiredArgsConstructor
public class LogController {

    private final LogService logService;

    /**
     * 获取登录日志
     *
     * @param username  用户名
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param pageNum   页码
     * @param pageSize  每页大小
     * @return 登录日志分页结果
     */
    @Operation(summary = "获取登录日志", description = "分页查询登录日志")
    @Parameters({
            @Parameter(name = "username", description = "用户名", in = ParameterIn.QUERY),
            @Parameter(name = "startTime", description = "开始时间", in = ParameterIn.QUERY),
            @Parameter(name = "endTime", description = "结束时间", in = ParameterIn.QUERY),
            @Parameter(name = "pageNum", description = "页码", in = ParameterIn.QUERY),
            @Parameter(name = "pageSize", description = "每页大小", in = ParameterIn.QUERY)
    })
    @GetMapping("/login")
    @PreAuthorize("hasAuthority('" + AUTH_LOG_QUERY + "')")
    public Result<PageVO<LoginLogVO>> getLoginLogs(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        PageVO<LoginLogVO> pageResult = logService.getLoginLogs(username, startTime, endTime, pageNum, pageSize);
        return Result.success(pageResult);
    }

    /**
     * 获取登录失败日志
     *
     * @param username  用户名
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param pageNum   页码
     * @param pageSize  每页大小
     * @return 登录失败日志分页结果
     */
    @Operation(summary = "获取登录失败日志", description = "分页查询登录失败记录")
    @Parameters({
            @Parameter(name = "username", description = "用户名", in = ParameterIn.QUERY),
            @Parameter(name = "startTime", description = "开始时间", in = ParameterIn.QUERY),
            @Parameter(name = "endTime", description = "结束时间", in = ParameterIn.QUERY),
            @Parameter(name = "pageNum", description = "页码", in = ParameterIn.QUERY),
            @Parameter(name = "pageSize", description = "每页大小", in = ParameterIn.QUERY)
    })
    @GetMapping("/login/fail")
    @PreAuthorize("hasAuthority('" + AUTH_LOG_QUERY + "')")
    public Result<PageVO<LoginFailVO>> getLoginFailLogs(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        PageVO<LoginFailVO> pageResult = logService.getLoginFailLogs(username, startTime, endTime, pageNum, pageSize);
        return Result.success(pageResult);
    }
} 