package com.hngy.siae.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hngy.siae.user.entity.MajorClassEnrollment;
import org.apache.ibatis.annotations.Mapper;

/**
 * 专业班级学生关联表 Mapper 接口
 *
 * @author KEYKB
 */
@Mapper
public interface MajorClassEnrollmentMapper extends BaseMapper<MajorClassEnrollment> {
}
