package com.hngy.siae.user.dto.response;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户完整信息视图对象
 * <p>
 * 包含用户基本信息和用户详情信息的完整视图对象，用于用户创建后的返回值。
 * 整合了 user 表和 user_profile 表的数据。
 *
 * @author KEYKB
 */
@Data
public class UserWithProfileVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    // ==================== 用户基本信息（来自 user 表） ====================

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 状态：1启用，0禁用
     */
    private Integer status;

    /**
     * 状态名称
     */
    private String statusName;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    // ==================== 用户详情信息（来自 user_profile 表） ====================

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
     * 用户头像URL
     */
    private String avatar;

    /**
     * 个人简介
     */
    private String bio;

    /**
     * 性别：0未知，1男，2女
     */
    private Integer gender;

    /**
     * 性别名称
     */
    private String genderName;

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
     * 主页背景图片URL
     */
    private String bgUrl;

    // ==================== 班级关联信息（可选，来自 class_user 表） ====================

    /**
     * 班级ID
     */
    private Long classId;

    /**
     * 班级名称
     */
    private String className;

    /**
     * 成员类型：0非协会成员，1预备成员，2正式成员
     */
    private Integer memberType;

    /**
     * 成员类型名称
     */
    private String memberTypeName;

    /**
     * 入会日期
     */
    private LocalDate joinDate;
}
