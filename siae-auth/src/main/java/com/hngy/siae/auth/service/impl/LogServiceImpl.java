package com.hngy.siae.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hngy.siae.auth.dto.LoginFailResponse;
import com.hngy.siae.auth.dto.LoginLogResponse;
import com.hngy.siae.auth.dto.PageResult;
import com.hngy.siae.auth.entity.LoginLog;
import com.hngy.siae.auth.mapper.LoginLogMapper;
import com.hngy.siae.auth.service.LogService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 日志服务实现类
 * 
 * @author KEYKB
 */
@Service
public class LogServiceImpl implements LogService {
    
    private final LoginLogMapper loginLogMapper;
    
    /**
     * 构造函数
     *
     * @param loginLogMapper 登录日志Mapper
     */
    public LogServiceImpl(LoginLogMapper loginLogMapper) {
        this.loginLogMapper = loginLogMapper;
    }
    
    @Override
    public PageResult<LoginLogResponse> getLoginLogs(LocalDateTime startTime, LocalDateTime endTime,
                                                   String username, Integer status, int page, int size) {
        // 构建查询条件
        LambdaQueryWrapper<LoginLog> queryWrapper = new LambdaQueryWrapper<>();
        if (startTime != null) {
            queryWrapper.ge(LoginLog::getLoginTime, startTime);
        }
        if (endTime != null) {
            queryWrapper.le(LoginLog::getLoginTime, endTime);
        }
        if (StringUtils.hasText(username)) {
            queryWrapper.like(LoginLog::getUsername, username);
        }
        if (status != null) {
            queryWrapper.eq(LoginLog::getStatus, status);
        }
        queryWrapper.orderByDesc(LoginLog::getLoginTime);
        
        // 分页查询
        IPage<LoginLog> pageResult = loginLogMapper.selectPage(new Page<>(page, size), queryWrapper);
        
        // 转换结果
        List<LoginLogResponse> loginLogResponses = pageResult.getRecords().stream()
                .map(this::convertToLoginLogResponse)
                .collect(Collectors.toList());
        
        return PageResult.of(loginLogResponses, pageResult.getTotal());
    }
    
    @Override
    public PageResult<LoginFailResponse> getLoginFailLogs(LocalDateTime startTime, LocalDateTime endTime,
                                                       String username, String loginIp, int page, int size) {
        // 构建查询条件
        LambdaQueryWrapper<LoginLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(LoginLog::getStatus, 0);  // 只查询失败日志
        if (startTime != null) {
            queryWrapper.ge(LoginLog::getLoginTime, startTime);
        }
        if (endTime != null) {
            queryWrapper.le(LoginLog::getLoginTime, endTime);
        }
        if (StringUtils.hasText(username)) {
            queryWrapper.like(LoginLog::getUsername, username);
        }
        if (StringUtils.hasText(loginIp)) {
            queryWrapper.like(LoginLog::getLoginIp, loginIp);
        }
        queryWrapper.orderByDesc(LoginLog::getLoginTime);
        
        // 分页查询
        IPage<LoginLog> pageResult = loginLogMapper.selectPage(new Page<>(page, size), queryWrapper);
        
        // 转换结果
        List<LoginFailResponse> loginFailResponses = pageResult.getRecords().stream()
                .map(this::convertToLoginFailResponse)
                .collect(Collectors.toList());
        
        return PageResult.of(loginFailResponses, pageResult.getTotal());
    }
    
    /**
     * 将登录日志实体转换为登录日志响应
     *
     * @param loginLog 登录日志实体
     * @return 登录日志响应
     */
    private LoginLogResponse convertToLoginLogResponse(LoginLog loginLog) {
        LoginLogResponse response = new LoginLogResponse();
        response.setId(loginLog.getId());
        response.setUserId(loginLog.getUserId());
        response.setUsername(loginLog.getUsername());
        response.setLoginIp(loginLog.getLoginIp());
        response.setLoginLocation(loginLog.getLoginLocation());
        response.setBrowser(loginLog.getBrowser());
        response.setOs(loginLog.getOs());
        response.setStatus(loginLog.getStatus());
        response.setMsg(loginLog.getMsg());
        response.setLoginTime(loginLog.getLoginTime());
        return response;
    }
    
    /**
     * 将登录日志实体转换为登录失败响应
     *
     * @param loginLog 登录日志实体
     * @return 登录失败响应
     */
    private LoginFailResponse convertToLoginFailResponse(LoginLog loginLog) {
        LoginFailResponse response = new LoginFailResponse();
        response.setId(loginLog.getId());
        response.setUserId(loginLog.getUserId());
        response.setUsername(loginLog.getUsername());
        response.setLoginIp(loginLog.getLoginIp());
        response.setFailReason(loginLog.getMsg());
        response.setFailTime(loginLog.getLoginTime());
        return response;
    }
} 