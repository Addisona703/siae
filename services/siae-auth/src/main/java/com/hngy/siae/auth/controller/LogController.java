package com.hngy.siae.auth.controller;

import com.hngy.siae.auth.dto.request.LoginQueryDTO;
import com.hngy.siae.auth.dto.response.LoginFailVO;
import com.hngy.siae.auth.dto.response.LoginLogVO;
import com.hngy.siae.auth.service.LogService;
import com.hngy.siae.core.dto.PageDTO;
import com.hngy.siae.core.dto.PageVO;
import com.hngy.siae.core.result.Result;
import com.hngy.siae.security.annotation.SiaeAuthorize;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
     * @param pageDTO 分页查询参数
     * @return 登录日志分页结果
     */
    @Operation(summary = "获取登录日志", description = "分页查询登录日志")
    @PostMapping("/login")
    @SiaeAuthorize("hasAuthority('" + AUTH_LOG_QUERY + "')")
    public Result<PageVO<LoginLogVO>> getLoginLogs(@Valid @RequestBody PageDTO<LoginQueryDTO> pageDTO) {
        PageVO<LoginLogVO> pageResult = logService.getLoginLogs(pageDTO);
        return Result.success(pageResult);
    }

    /**
     * 获取登录失败日志
     *
     * @param pageDTO 分页查询参数
     * @return 登录失败日志分页结果
     */
    @Operation(summary = "获取登录失败日志", description = "分页查询登录失败记录")
    @PostMapping("/login/fail")
    @SiaeAuthorize("hasAuthority('" + AUTH_LOG_QUERY + "')")
    public Result<PageVO<LoginFailVO>> getLoginFailLogs(@Valid @RequestBody PageDTO<LoginQueryDTO> pageDTO) {
        PageVO<LoginFailVO> pageResult = logService.getLoginFailLogs(pageDTO);
        return Result.success(pageResult);
    }
} 