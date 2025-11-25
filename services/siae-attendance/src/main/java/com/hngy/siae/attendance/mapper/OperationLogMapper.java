package com.hngy.siae.attendance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hngy.siae.attendance.entity.OperationLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 操作日志 Mapper
 *
 * @author SIAE Team
 */
@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLog> {
}
