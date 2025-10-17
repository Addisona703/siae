package com.hngy.siae.auth.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hngy.siae.auth.dto.request.DashboardQueryDTO;
import com.hngy.siae.auth.dto.request.LoginQueryDTO;
import com.hngy.siae.auth.dto.response.DashboardStatsVO;
import com.hngy.siae.auth.dto.response.LoginFailVO;
import com.hngy.siae.auth.dto.response.LoginLogVO;
import com.hngy.siae.auth.entity.LoginLog;
import com.hngy.siae.core.dto.PageDTO;
import com.hngy.siae.core.dto.PageVO;

/**
 * 日志服务接口
 *
 * @author KEYKB
 */
public interface LogService extends IService<LoginLog> {

    /**
     * 获取登录日志（统一查询接口，支持按用户名、状态筛选）
     *
     * @param pageDTO 分页查询参数
     * @return 登录日志分页结果
     */
    PageVO<LoginLogVO> getLoginLogs(PageDTO<LoginQueryDTO> pageDTO);

    /**
     * 获取登录失败日志
     *
     * @param pageDTO 分页查询参数
     * @return 登录失败日志分页结果
     */
    PageVO<LoginFailVO> getLoginFailLogs(PageDTO<LoginQueryDTO> pageDTO);

    /**
     * 获取仪表盘统计数据
     *
     * @param queryDTO 统计查询参数
     * @return 仪表盘统计结果
     */
    DashboardStatsVO getDashboardStats(DashboardQueryDTO queryDTO);

    /**
     * 异步保存登录日志
     *
     * @param userId   用户ID
     * @param username 用户名
     * @param clientIp 客户端IP
     * @param browser  浏览器信息
     * @param os       操作系统信息
     * @param status   登录状态：1成功，0失败
     * @param message  登录消息
     */
    void saveLoginLogAsync(Long userId, String username, String clientIp, String browser, String os, Integer status, String message);
}