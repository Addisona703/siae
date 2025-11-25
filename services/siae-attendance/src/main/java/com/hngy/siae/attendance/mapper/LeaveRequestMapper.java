package com.hngy.siae.attendance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hngy.siae.attendance.entity.LeaveRequest;
import org.apache.ibatis.annotations.Mapper;

/**
 * 请假申请 Mapper
 *
 * @author SIAE Team
 */
@Mapper
public interface LeaveRequestMapper extends BaseMapper<LeaveRequest> {
}
