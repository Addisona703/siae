package com.hngy.siae.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hngy.siae.user.dto.response.ClassInfoVO;
import com.hngy.siae.user.entity.ClassInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 班级表 Mapper 接口
 * 
 * @author AI开发助手
 */
@Mapper
public interface ClassInfoMapper extends BaseMapper<ClassInfo> {

    /**
     * 根据ID查询班级详情（包含学院、专业名称）
     *
     * @param id 班级ID
     * @return 班级详情
     */
    @Select("""
            SELECT
                c.id,
                c.college_id AS collegeId,
                col.name      AS collegeName,
                c.major_id    AS majorId,
                m.name        AS majorName,
                m.abbr        AS majorAbbr,
                c.year,
                c.class_no    AS classNo,
                c.created_at  AS createTime,
                c.updated_at  AS updateTime
            FROM `class` c
            LEFT JOIN college col ON col.id = c.college_id
            LEFT JOIN major m ON m.id = c.major_id
            WHERE c.id = #{id}
              AND c.is_deleted = 0
            """)
    ClassInfoVO selectClassDetailById(@Param("id") Long id);
} 
