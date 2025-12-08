package com.hngy.siae.auth.controller;

import com.hngy.siae.auth.dto.request.DashboardQueryDTO;
import com.hngy.siae.auth.dto.request.LoginQueryDTO;
import com.hngy.siae.auth.dto.response.DashboardStatsVO;
import com.hngy.siae.auth.dto.response.LoginFailVO;
import com.hngy.siae.auth.dto.response.LoginLogVO;
import com.hngy.siae.auth.service.LogService;
import com.hngy.siae.core.dto.PageDTO;
import com.hngy.siae.core.dto.PageVO;
import com.hngy.siae.core.result.Result;
import com.hngy.siae.security.annotation.SiaeAuthorize;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import static com.hngy.siae.auth.permissions.AuthPermissions.*;

/**
 * 日志控制器
 * <p>
 * 提供登录日志查询、统计分析等功能
 *
 * @author KEYKB
 */
@Tag(name = "日志管理", description = "日志查询与统计相关API")
@RestController
@RequestMapping("/logs")
@RequiredArgsConstructor
public class LogController {

    private final LogService logService;

    /**
     * 获取登录日志（统一查询接口）
     * <p>
     * 支持按用户名、时间范围、登录状态筛选
     * <p>
     * 筛选规则：
     * <ul>
     *   <li>不传 status：查询所有日志</li>
     *   <li>status = 1：只查询登录成功的日志</li>
     *   <li>status = 0：只查询登录失败的日志</li>
     * </ul>
     *
     * @param pageDTO 分页查询参数
     * @return 登录日志分页结果
     */
    @Operation(
            summary = "获取登录日志",
            description = "分页查询登录日志，支持按用户名、时间范围、登录状态筛选（status: 1成功/0失败/不传查全部）"
    )
    @PostMapping("/login")
    @SiaeAuthorize("hasAuthority('" + AUTH_LOG_QUERY + "')")
    public Result<PageVO<LoginLogVO>> getLoginLogs(
            @Parameter(description = "分页查询参数，params.username支持模糊查询，params.status筛选状态（1成功/0失败/不传全部）")
            @Valid @RequestBody PageDTO<LoginQueryDTO> pageDTO) {
        PageVO<LoginLogVO> pageResult = logService.getLoginLogs(pageDTO);
        return Result.success(pageResult);
    }

    /**
     * 获取仪表盘统计数据
     * <p>
     * 统计指定天数内的登录人数（日活量）和注册人数
     * <p>
     * 支持的统计周期：
     * <ul>
     *   <li>7天：最近一周的数据</li>
     *   <li>30天：最近一个月的数据</li>
     *   <li>90天：最近三个月的数据</li>
     * </ul>
     *
     * @param days 统计天数（7、30、90）
     * @return 仪表盘统计结果，包含每日数据和汇总数据
     */
    @Operation(
            summary = "获取仪表盘统计数据",
            description = "统计指定天数内的登录人数（日活量）和注册人数，支持7/30/90天"
    )
    @GetMapping("/dashboard/stats/{days}")
    @SiaeAuthorize("hasAuthority('" + AUTH_LOG_QUERY + "')")
    public Result<DashboardStatsVO> getDashboardStats(
            @Parameter(description = "统计天数（7、30、90）", example = "7")
            @PathVariable Integer days) {
        DashboardQueryDTO queryDTO = new DashboardQueryDTO();
        queryDTO.setDays(days);
        DashboardStatsVO stats = logService.getDashboardStats(queryDTO);
        return Result.success(stats);
    }
} 