package com.hngy.siae.auth.service;

import com.hngy.siae.auth.dto.response.LoginFailVO;
import com.hngy.siae.auth.dto.response.LoginLogVO;
import com.hngy.siae.common.dto.response.PageVO;

import java.time.LocalDateTime;

/**
 * 日志服务接口
 * 
 * @author KEYKB
 */
public interface LogService {
    
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
    PageVO<LoginLogVO> getLoginLogs(String username, LocalDateTime startTime, LocalDateTime endTime,
                                    Integer pageNum, Integer pageSize);
    
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
    PageVO<LoginFailVO> getLoginFailLogs(String username, LocalDateTime startTime, LocalDateTime endTime,
                                         Integer pageNum, Integer pageSize);
} 