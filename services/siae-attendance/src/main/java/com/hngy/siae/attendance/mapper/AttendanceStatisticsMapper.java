package com.hngy.siae.attendance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hngy.siae.attendance.entity.AttendanceStatistics;
import org.apache.ibatis.annotations.Mapper;

/**
 * 考勤统计 Mapper
 *
 * @author SIAE Team
 */
@Mapper
public interface AttendanceStatisticsMapper extends BaseMapper<AttendanceStatistics> {
}
