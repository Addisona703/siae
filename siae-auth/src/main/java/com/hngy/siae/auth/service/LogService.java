package com.hngy.siae.auth.service;

import com.hngy.siae.auth.dto.LoginFailResponse;
import com.hngy.siae.auth.dto.LoginLogResponse;
import com.hngy.siae.auth.dto.PageResult;

import java.time.LocalDateTime;

/**
 * 日志服务接口
 * 
 * @author KEYKB
 */
public interface LogService {
    
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
    PageResult<LoginLogResponse> getLoginLogs(LocalDateTime startTime, LocalDateTime endTime,
                                             String username, Integer status, int page, int size);
    
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
    PageResult<LoginFailResponse> getLoginFailLogs(LocalDateTime startTime, LocalDateTime endTime,
                                                String username, String loginIp, int page, int size);
} 