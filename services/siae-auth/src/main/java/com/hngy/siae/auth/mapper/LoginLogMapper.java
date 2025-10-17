package com.hngy.siae.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hngy.siae.auth.entity.LoginLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 登录日志Mapper接口
 *
 * @author KEYKB
 */
@Mapper
public interface LoginLogMapper extends BaseMapper<LoginLog> {

    /**
     * 统计指定时间范围内每天的登录人数
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 每日登录人数统计（date, count）
     */
    @Select("SELECT DATE(login_time) as date, COUNT(DISTINCT user_id) as count " +
            "FROM login_log " +
            "WHERE status = 1 " +
            "AND login_time >= #{startDate} " +
            "AND login_time < #{endDate} " +
            "GROUP BY DATE(login_time) " +
            "ORDER BY date")
    List<Map<String, Object>> countDailyLogins(@Param("startDate") LocalDateTime startDate,
                                                 @Param("endDate") LocalDateTime endDate);

    /**
     * 统计指定时间范围内每天的注册人数
     * 注意：这里通过 msg 字段判断是否为注册日志
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 每日注册人数统计（date, count）
     */
    @Select("SELECT DATE(login_time) as date, COUNT(DISTINCT user_id) as count " +
            "FROM login_log " +
            "WHERE status = 1 " +
            "AND msg LIKE '%注册%' " +
            "AND login_time >= #{startDate} " +
            "AND login_time < #{endDate} " +
            "GROUP BY DATE(login_time) " +
            "ORDER BY date")
    List<Map<String, Object>> countDailyRegisters(@Param("startDate") LocalDateTime startDate,
                                                    @Param("endDate") LocalDateTime endDate);
} 