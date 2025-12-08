package com.hngy.siae.user.dto.response;

import com.hngy.siae.user.enums.GenderEnum;
import com.hngy.siae.user.enums.MemberTypeEnum;
import com.hngy.siae.user.enums.UserStatusEnum;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户详细信息视图对象
 * <p>
 * 包含用户基本信息、用户详情信息和班级关联信息的综合视图
 *
 * @author KEYKB
 */
@Data
public class UserDetailVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    // ==================== 用户基本信息（user表） ====================

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 学号
     */
    private String studentId;

    /**
     * 头像文件ID
     */
    private String avatarFileId;

    /**
     * 头像访问URL（从Media服务获取）
     */
    private String avatarUrl;

    /**
     * 状态：0禁用，1启用
     */
    private UserStatusEnum status;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    // ==================== 用户详情信息（user_profile表） ====================

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号码
     */
    private String phone;

    /**
     * 个人简介
     */
    private String bio;

    /**
     * 性别：0未知，1男，2女
     */
    private GenderEnum gender;

    /**
     * 出生日期
     */
    private LocalDate birthday;

    /**
     * 身份证号
     */
    private String idCard;

    /**
     * QQ号
     */
    private String qq;

    /**
     * 微信号
     */
    private String wechat;

    /**
     * 主页背景文件ID
     */
    private String backgroundFileId;

    /**
     * 主页背景访问URL（从Media服务获取）
     */
    private String backgroundUrl;

    // ==================== 班级关联信息（major_class_enrollment表） ====================

    /**
     * 专业ID
     */
    private Long majorId;

    /**
     * 专业名称
     */
    private String majorName;

    /**
     * 入学年份
     */
    private Integer entryYear;

    /**
     * 班号
     */
    private Integer classNo;

    /**
     * 班级名称（格式：专业简称+年份后两位-班号，如：软工21-1班）
     */
    private String className;

    /**
     * 成员类型：0非协会成员，1协会成员
     */
    private MemberTypeEnum memberType;

    /**
     * 入会日期
     */
    private LocalDate joinDate;
}
