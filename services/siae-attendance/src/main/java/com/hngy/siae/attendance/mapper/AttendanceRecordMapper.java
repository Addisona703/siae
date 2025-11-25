package com.hngy.siae.attendance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hngy.siae.attendance.entity.AttendanceRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 考勤记录 Mapper
 *
 * @author SIAE Team
 */
@Mapper
public interface AttendanceRecordMapper extends BaseMapper<AttendanceRecord> {
}
