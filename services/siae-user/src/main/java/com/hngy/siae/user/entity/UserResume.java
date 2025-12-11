package com.hngy.siae.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户简历实体类
 * 存储用户的简历信息，每个用户只能有一份简历
 * 复杂字段使用 JSON 格式存储
 *
 * @author KEYKB
 */
@Data
@TableName("user_resume")
public class UserResume implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键，自增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID，关联user表
     */
    private Long userId;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 姓名
     */
    private String name;

    /**
     * 性别
     */
    private String gender;

    /**
     * 年龄
     */
    private Integer age;

    /**
     * 工作状态
     */
    private String workStatus;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 微信号
     */
    private String wechat;

    /**
     * 求职状态
     */
    private String jobStatus;

    /**
     * 毕业年份
     */
    private String graduationYear;

    /**
     * 期望职位列表（JSON格式）
     */
    private String expectedJobs;

    /**
     * 个人优势
     */
    private String advantages;

    /**
     * 工作经历列表（JSON格式）
     */
    private String workExperience;

    /**
     * 项目经验列表（JSON格式）
     */
    private String projects;

    /**
     * 教育经历列表（JSON格式）
     */
    private String education;

    /**
     * 获奖情况列表（JSON格式）
     */
    private String awards;

    /**
     * 是否逻辑删除：0否，1是
     */
    @TableLogic
    private Integer isDeleted;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
