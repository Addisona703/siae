package com.hngy.siae.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hngy.siae.auth.dto.request.LoginQueryDTO;
import com.hngy.siae.auth.dto.response.LoginFailVO;
import com.hngy.siae.auth.dto.response.LoginLogVO;
import com.hngy.siae.auth.entity.LoginLog;
import com.hngy.siae.auth.mapper.LoginLogMapper;
import com.hngy.siae.auth.service.LogService;
import com.hngy.siae.core.asserts.AssertUtils;
import com.hngy.siae.core.dto.PageDTO;
import com.hngy.siae.core.dto.PageVO;
import com.hngy.siae.core.utils.BeanConvertUtil;
import com.hngy.siae.web.utils.PageConvertUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 日志服务实现类
 * <p>
 * 提供登录日志的查询和异步记录功能，
 * 支持安全审计和用户行为分析。
 *
 * @author KEYKB
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LogServiceImpl
        extends ServiceImpl<LoginLogMapper, LoginLog>
        implements LogService {
    
    /**
     * 分页查询登录日志
     * <p>
     * 支持按时间、用户名、状态等条件筛选登录记录。
     *
     * @param pageDTO 分页查询参数
     * @return 分页的登录日志列表
     */
    @Override
    public PageVO<LoginLogVO> getLoginLogs(PageDTO<LoginQueryDTO> pageDTO) {
        // 构建查询条件，使用Optional链式调用
        LoginQueryDTO query = pageDTO.getParams();
        LambdaQueryWrapper<LoginLog> queryWrapper = Wrappers.lambdaQuery();

        if (query != null) {
            queryWrapper
                    .ge(query.getStartTime() != null, LoginLog::getLoginTime, query.getStartTime())
                    .le(query.getEndTime() != null, LoginLog::getLoginTime, query.getEndTime())
                    .like(StringUtils.hasText(query.getUsername()), LoginLog::getUsername, query.getUsername())
                    .eq(query.getStatus() != null, LoginLog::getStatus, query.getStatus())
                    .orderByDesc(LoginLog::getLoginTime);
        }

        // 分页查询
        IPage<LoginLog> page = page(PageConvertUtil.toPage(pageDTO), queryWrapper);
        return PageConvertUtil.convert(page, LoginLogVO.class);
    }
    
    /**
     * 分页查询登录失败日志
     * <p>
     * 专门查询失败的登录记录，用于安全审计和风险分析。
     *
     * @param pageDTO 分页查询参数
     * @return 分页的登录失败日志列表
     */
    @Override
    public PageVO<LoginFailVO> getLoginFailLogs(PageDTO<LoginQueryDTO> pageDTO) {
        // 构建查询条件，使用Optional链式调用
        LoginQueryDTO query = Optional.ofNullable(pageDTO.getParams()).orElse(new LoginQueryDTO());

        LambdaQueryWrapper<LoginLog> queryWrapper = Wrappers.lambdaQuery();

        queryWrapper
                .eq(LoginLog::getStatus, 0)  // 只查询失败日志
                .ge(query.getStartTime() != null, LoginLog::getLoginTime, query.getStartTime())
                .le(query.getEndTime() != null, LoginLog::getLoginTime, query.getEndTime())
                .like(StringUtils.hasText(query.getUsername()), LoginLog::getUsername, query.getUsername())
                .orderByDesc(LoginLog::getLoginTime);

        // 分页查询
        IPage<LoginLog> page = page(PageConvertUtil.toPage(pageDTO), queryWrapper);
        return PageConvertUtil.convert(page, this::convertToLoginFailResponse);
    }

    /**
     * 将登录日志实体转换为登录失败响应
     *
     * @param loginLog 登录日志实体
     * @return 登录失败响应对象
     */
    private LoginFailVO convertToLoginFailResponse(LoginLog loginLog) {
        LoginFailVO response = BeanConvertUtil.to(loginLog, LoginFailVO.class);
        // 设置特殊字段映射
        response.setFailReason(loginLog.getMsg());
        response.setFailTime(loginLog.getLoginTime());
        return response;
    }

    /**
     * 异步保存登录日志
     * <p>
     * 异步记录用户登录日志，不影响主业务流程性能。
     *
     * @param userId 用户ID
     * @param username 用户名
     * @param clientIp 客户端IP地址
     * @param browser 浏览器信息
     * @param os 操作系统信息
     * @param status 登录状态（1成功，0失败）
     * @param message 登录结果消息
     */
    @Override
    @Async
    public void saveLoginLogAsync(Long userId, String username, String clientIp, String browser, String os, Integer status, String message) {
        try {
            // 参数验证
            AssertUtils.notEmpty(username, "用户名不能为空");
            AssertUtils.notNull(status, "登录状态不能为空");

            // 创建登录日志实体
            LoginLog loginLog = new LoginLog();
            loginLog.setUserId(userId);
            loginLog.setUsername(username);
            loginLog.setLoginIp(clientIp != null ? clientIp : "未知");
            loginLog.setBrowser(browser != null ? browser : "未知");
            loginLog.setOs(os != null ? os : "未知");
            loginLog.setStatus(status);
            loginLog.setMsg(message != null ? message : "");
            loginLog.setLoginTime(LocalDateTime.now());
            
            // 保存到数据库
            save(loginLog);

            log.debug("异步保存登录日志成功，用户: {}, 状态: {}, 消息: {}", username, status, message);

        } catch (Exception e) {
            log.error("异步保存登录日志失败，用户: {}, 状态: {}, 消息: {}", username, status, message, e);
            // 不抛出异常，避免影响主业务流程
        }
    }
}