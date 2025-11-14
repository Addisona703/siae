package com.hngy.siae.user.dto.response;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 成员详细信息视图对象
 * 包含用户信息、部门信息、职位信息
 *
 * @author KEYKB
 */
@Data
public class MembershipDetailVO {

    /**
     * 成员ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 学号
     */
    private String studentId;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 用户头像文件ID
     */
    private String avatarFileId;

    /**
     * 成员大头照文件ID
     */
    private String headshotFileId;

    /**
     * 用户头像访问URL（从Media服务获取）
     */
    private String avatarUrl;

    /**
     * 成员大头照访问URL（从Media服务获取）
     */
    private String headshotUrl;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 生命周期状态：0候选，1正式
     */
    private Integer lifecycleStatus;

    /**
     * 生命周期状态名称
     */
    private String lifecycleStatusName;

    /**
     * 加入日期（成为正式成员的日期）
     */
    private LocalDate joinDate;

    /**
     * 个人简介
     */
    private String bio;

    /**
     * 年级（入学年份）
     */
    private Integer entryYear;

    /**
     * 专业名称
     */
    private String majorName;

    /**
     * 在校状态：1在读，2离校
     */
    private Integer enrollmentStatus;

    /**
     * 所属部门列表
     */
    private List<MemberDepartmentVO> departments;

    /**
     * 担任职位列表
     */
    private List<MemberPositionVO> positions;

    /**
     * 荣誉成就列表（奖项名称）
     */
    private List<String> awards;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 设置生命周期状态并自动设置状态名称
     */
    public void setLifecycleStatus(Integer lifecycleStatus) {
        this.lifecycleStatus = lifecycleStatus;
        if (lifecycleStatus != null) {
            this.lifecycleStatusName = lifecycleStatus == 0 ? "候选成员" : "正式成员";
        }
    }
}
