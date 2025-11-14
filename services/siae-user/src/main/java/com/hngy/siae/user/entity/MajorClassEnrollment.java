package com.hngy.siae.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hngy.siae.user.enums.ClassUserStatusEnum;
import com.hngy.siae.user.enums.MemberTypeEnum;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 专业班级学生关联表实体类
 *
 * @author KEYKB
 */
@Data
@TableName("major_class_enrollment")
public class MajorClassEnrollment implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键，自增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 专业ID
     */
    private Long majorId;

    /**
     * 入学年份
     */
    private Integer entryYear;

    /**
     * 班号
     */
    private Integer classNo;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 成员类型：0非协会成员，1协会成员
     */
    private MemberTypeEnum memberType;

    /**
     * 状态：1在读，2离校
     */
    private ClassUserStatusEnum status;

    /**
     * 入会日期
     */
    private LocalDate joinDate;

    /**
     * 是否删除：0否，1是
     */
    @TableLogic
    private Integer isDeleted;

    /**
     * 记录创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 记录更新时间
     */
    private LocalDateTime updatedAt;
}
