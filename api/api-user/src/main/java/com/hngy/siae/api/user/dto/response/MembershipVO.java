package com.hngy.siae.api.user.dto.response;

import com.hngy.siae.api.user.enums.LifecycleStatusEnum;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 成员信息视图对象
 *
 * @author KEYKB
 */
@Data
public class MembershipVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 成员ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 成员大头照文件ID
     */
    private String headshotFileId;

    /**
     * 成员大头照访问URL（从Media服务获取）
     */
    private String headshotUrl;

    /**
     * 生命周期状态：0候选，1正式
     */
    private LifecycleStatusEnum lifecycleStatus;

    /**
     * 加入日期（成为正式成员的日期）
     */
    private LocalDate joinDate;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 部门名称
     */
    private String departmentName;

    /**
     * 职位名称
     */
    private String positionName;

    /**
     * 年级（入学年份）
     */
    private Integer entryYear;

    /**
     * 专业名称
     */
    private String majorName;

    /**
     * 个人简介
     */
    private String bio;

    /**
     * 在校状态：1在读，2离校
     */
    private Integer enrollmentStatus;
}
