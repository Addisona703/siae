package com.hngy.siae.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hngy.siae.auth.dto.response.LoginFailVO;
import com.hngy.siae.auth.dto.response.LoginLogVO;
import com.hngy.siae.common.dto.response.PageVO;
import com.hngy.siae.auth.entity.LoginLog;
import com.hngy.siae.auth.mapper.LoginLogMapper;
import com.hngy.siae.auth.service.LogService;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class LogServiceImpl implements LogService {
    
    private final LoginLogMapper loginLogMapper;
    
    @Override
    public PageVO<LoginLogVO> getLoginLogs(String username, LocalDateTime startTime, LocalDateTime endTime,
                                           Integer pageNum, Integer pageSize) {
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
        queryWrapper.orderByDesc(LoginLog::getLoginTime);
        
        // 分页查询
        IPage<LoginLog> page = loginLogMapper.selectPage(new Page<>(pageNum, pageSize), queryWrapper);
        
        // 转换结果
        List<LoginLogVO> loginLogVOs = page.getRecords().stream()
                .map(this::convertToLoginLogResponse)
                .collect(Collectors.toList());
        
        // 创建并返回PageVO
        PageVO<LoginLogVO> pageVO = new PageVO<>();
        pageVO.setTotal(page.getTotal());
        pageVO.setPageNum(pageNum);
        pageVO.setPageSize(pageSize);
        pageVO.setRecords(loginLogVOs);
        
        return pageVO;
    }
    
    @Override
    public PageVO<LoginFailVO> getLoginFailLogs(String username, LocalDateTime startTime, LocalDateTime endTime,
                                                Integer pageNum, Integer pageSize) {
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
        queryWrapper.orderByDesc(LoginLog::getLoginTime);
        
        // 分页查询
        IPage<LoginLog> page = loginLogMapper.selectPage(new Page<>(pageNum, pageSize), queryWrapper);
        
        // 转换结果
        List<LoginFailVO> loginFailVOs = page.getRecords().stream()
                .map(this::convertToLoginFailResponse)
                .collect(Collectors.toList());
        
        // 创建并返回PageVO
        PageVO<LoginFailVO> pageVO = new PageVO<>();
        pageVO.setTotal(page.getTotal());
        pageVO.setPageNum(pageNum);
        pageVO.setPageSize(pageSize);
        pageVO.setRecords(loginFailVOs);
        
        return pageVO;
    }
    
    /**
     * 将登录日志实体转换为登录日志响应
     *
     * @param loginLog 登录日志实体
     * @return 登录日志响应
     */
    private LoginLogVO convertToLoginLogResponse(LoginLog loginLog) {
        LoginLogVO response = new LoginLogVO();
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
    private LoginFailVO convertToLoginFailResponse(LoginLog loginLog) {
        LoginFailVO response = new LoginFailVO();
        response.setId(loginLog.getId());
        response.setUserId(loginLog.getUserId());
        response.setUsername(loginLog.getUsername());
        response.setLoginIp(loginLog.getLoginIp());
        response.setFailReason(loginLog.getMsg());
        response.setFailTime(loginLog.getLoginTime());
        return response;
    }
} 