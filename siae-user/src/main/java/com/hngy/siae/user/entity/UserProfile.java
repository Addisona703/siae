package com.hngy.siae.user.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户详情表实体类
 * 存储用户的扩展信息，与 user 表一对一关联
 *
 * @author KEYKB
 */
@Data
@TableName("user_profile")
public class UserProfile implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 外键，关联user表
     */
    @TableId
    private Long userId;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 个人简介
     */
    private String bio;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机
     */
    private String phone;

    /**
     * QQ号 (用于联系)
     */
    private String qq;

    /**
     * 微信号 (用于联系)
     */
    private String wechat;

    /**
     * 身份证号
     */
    private String idCard;

    /**
     * 性别：0未知，1男，2女
     */
    private Integer gender;

    /**
     * 出生日期
     */
    private LocalDate birthday;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
} 