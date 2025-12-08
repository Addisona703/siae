package com.hngy.siae.auth.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hngy.siae.auth.dto.request.DashboardQueryDTO;
import com.hngy.siae.auth.dto.request.LoginQueryDTO;
import com.hngy.siae.auth.dto.response.DailyStatsVO;
import com.hngy.siae.auth.dto.response.DashboardStatsVO;
import com.hngy.siae.auth.dto.response.LoginFailVO;
import com.hngy.siae.auth.dto.response.LoginLogVO;
import com.hngy.siae.auth.entity.LoginLog;
import com.hngy.siae.auth.mapper.LoginLogMapper;
import com.hngy.siae.auth.service.LogService;
import com.hngy.siae.core.asserts.AssertUtils;
import com.hngy.siae.core.dto.PageDTO;
import com.hngy.siae.core.dto.PageVO;
import com.hngy.siae.core.utils.BeanConvertUtil;
import com.hngy.siae.core.utils.PageConvertUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigInteger;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 日志服务实现类
 * <p>
 * 提供登录日志的查询、统计和异步记录功能，
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

    private final LoginLogMapper loginLogMapper;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

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
     * 获取仪表盘统计数据
     * <p>
     * 统计指定天数内的登录人数（日活量）和注册人数，
     * 支持最近7天、30天、90天的数据统计。
     *
     * @param queryDTO 统计查询参数
     * @return 仪表盘统计结果
     */
    @Override
    public DashboardStatsVO getDashboardStats(DashboardQueryDTO queryDTO) {
        AssertUtils.notNull(queryDTO, "查询参数不能为空");
        AssertUtils.notNull(queryDTO.getDays(), "统计天数不能为空");

        Integer days = queryDTO.getDays();
        // 验证天数参数
        if (days != 7 && days != 30 && days != 90) {
            throw new IllegalArgumentException("统计天数只能是 7、30 或 90");
        }

        // 计算时间范围
        LocalDateTime endDate = LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.MIN); // 明天00:00:00
        LocalDateTime startDate = LocalDateTime.of(LocalDate.now().minusDays(days - 1), LocalTime.MIN); // N天前00:00:00

        log.debug("查询仪表盘统计数据，时间范围：{} 至 {}", startDate, endDate);

        // 查询每日登录人数
        List<Map<String, Object>> dailyLoginData = loginLogMapper.countDailyLogins(startDate, endDate);
        Map<String, Long> loginMap = convertToDateMap(dailyLoginData);

        // 查询每日注册人数
        List<Map<String, Object>> dailyRegisterData = loginLogMapper.countDailyRegisters(startDate, endDate);
        Map<String, Long> registerMap = convertToDateMap(dailyRegisterData);

        // 生成完整的日期列表（填充没有数据的日期）
        List<DailyStatsVO> dailyStats = new ArrayList<>();
        long totalLoginCount = 0;
        long totalRegisterCount = 0;

        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            String dateStr = date.format(DATE_FORMATTER);

            Long loginCount = loginMap.getOrDefault(dateStr, 0L);
            Long registerCount = registerMap.getOrDefault(dateStr, 0L);

            dailyStats.add(DailyStatsVO.builder()
                    .date(dateStr)
                    .loginCount(loginCount)
                    .registerCount(registerCount)
                    .build());

            totalLoginCount += loginCount;
            totalRegisterCount += registerCount;
        }

        // 计算平均日活量
        long avgDailyLoginCount = totalLoginCount / days;

        return DashboardStatsVO.builder()
                .days(days)
                .totalLoginCount(totalLoginCount)
                .totalRegisterCount(totalRegisterCount)
                .avgDailyLoginCount(avgDailyLoginCount)
                .dailyStats(dailyStats)
                .build();
    }

    /**
     * 将数据库查询结果转换为日期-数量映射
     *
     * @param data 数据库查询结果
     * @return 日期-数量映射
     */
    private Map<String, Long> convertToDateMap(List<Map<String, Object>> data) {
        return data.stream()
                .collect(Collectors.toMap(
                        map -> {
                            Object dateObj = map.get("date");
                            if (dateObj instanceof java.sql.Date) {
                                return DateUtil.format(((Date) dateObj).toLocalDate().atStartOfDay(), "yyyy-MM-dd");
                            } else if (dateObj instanceof LocalDate) {
                                return ((LocalDate) dateObj).format(DATE_FORMATTER);
                            } else {
                                return dateObj.toString();
                            }
                        },
                        map -> {
                            Object countObj = map.get("count");
                            if (countObj instanceof Long) {
                                return (Long) countObj;
                            } else if (countObj instanceof BigInteger) {
                                return ((BigInteger) countObj).longValue();
                            } else if (countObj instanceof Integer) {
                                return ((Integer) countObj).longValue();
                            } else {
                                return Long.parseLong(countObj.toString());
                            }
                        }
                ));
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
     * @param userId   用户ID
     * @param username 用户名
     * @param clientIp 客户端IP地址
     * @param browser  浏览器信息
     * @param os       操作系统信息
     * @param status   登录状态（1成功，0失败）
     * @param message  登录结果消息
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