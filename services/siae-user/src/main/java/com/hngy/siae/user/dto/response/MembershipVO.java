package com.hngy.siae.user.dto.response;

import com.hngy.siae.user.enums.LifecycleStatusEnum;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 成员信息视图对象
 *
 * @author KEYKB
 */
@Data
public class MembershipVO {

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
     * 生命周期状态：0待审核，1候选，2正式，3已拒绝
     */
    private LifecycleStatusEnum lifecycleStatus;

    /**
     * 状态名称（待审核/候选成员/正式成员/已拒绝）
     */
    public String getStatusName() {
        return lifecycleStatus != null ? lifecycleStatus.getDescription() : null;
    }

    /**
     * 是否为正式成员
     */
    private Boolean isOfficial;

    /**
     * 是否为候选成员
     */
    private Boolean isCandidate;

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
