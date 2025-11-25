package com.hngy.siae.attendance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hngy.siae.attendance.dto.request.AttendanceRuleUpdateDTO;
import com.hngy.siae.attendance.entity.AttendanceRule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 考勤规则 Mapper
 *
 * @author SIAE Team
 */
@Mapper
public interface AttendanceRuleMapper extends BaseMapper<AttendanceRule> {

    /**
     * 动态更新考勤规则（只更新非空字段）
     *
     * @param id 规则ID
     * @param dto 更新DTO
     * @return 影响行数
     */
    int updateRuleSelective(@Param("id") Long id, @Param("dto") AttendanceRuleUpdateDTO dto);
}
