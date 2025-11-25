package com.hngy.siae.attendance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hngy.siae.attendance.entity.AttendanceAnomaly;
import org.apache.ibatis.annotations.Mapper;

/**
 * 考勤异常Mapper
 *
 * @author SIAE Team
 */
@Mapper
public interface AttendanceAnomalyMapper extends BaseMapper<AttendanceAnomaly> {
}
