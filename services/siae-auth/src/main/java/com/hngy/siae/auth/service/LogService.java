package com.hngy.siae.auth.service;

import com.hngy.siae.auth.dto.request.LoginQueryDTO;
import com.hngy.siae.auth.dto.response.LoginFailVO;
import com.hngy.siae.auth.dto.response.LoginLogVO;
import com.hngy.siae.core.dto.PageDTO;
import com.hngy.siae.core.dto.PageVO;

/**
 * 日志服务接口
 * 
 * @author KEYKB
 */
public interface LogService {
    
    /**
     * 获取登录日志
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