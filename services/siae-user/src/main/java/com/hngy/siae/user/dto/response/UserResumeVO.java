package com.hngy.siae.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户简历响应 VO
 *
 * @author KEYKB
 */
@Data
@Schema(description = "用户简历响应")
public class UserResumeVO {

    /**
     * 简历ID
     */
    @Schema(description = "简历ID")
    private Long id;

    /**
     * 用户ID
     */
    @Schema(description = "用户ID")
    private Long userId;

    /**
     * 头像URL
     */
    @Schema(description = "头像URL")
    private String avatar;

    /**
     * 姓名
     */
    @Schema(description = "姓名")
    private String name;

    /**
     * 性别
     */
    @Schema(description = "性别")
    private String gender;

    /**
     * 年龄
     */
    @Schema(description = "年龄")
    private Integer age;

    /**
     * 工作状态
     */
    @Schema(description = "工作状态")
    private String workStatus;

    /**
     * 手机号
     */
    @Schema(description = "手机号")
    private String phone;


    /**
     * 微信号
     */
    @Schema(description = "微信号")
    private String wechat;

    /**
     * 求职状态
     */
    @Schema(description = "求职状态")
    private String jobStatus;

    /**
     * 毕业年份
     */
    @Schema(description = "毕业年份")
    private String graduationYear;

    /**
     * 期望职位列表（JSON格式）
     */
    @Schema(description = "期望职位列表（JSON格式）")
    private String expectedJobs;

    /**
     * 个人优势
     */
    @Schema(description = "个人优势")
    private String advantages;

    /**
     * 工作经历列表（JSON格式）
     */
    @Schema(description = "工作经历列表（JSON格式）")
    private String workExperience;

    /**
     * 项目经验列表（JSON格式）
     */
    @Schema(description = "项目经验列表（JSON格式）")
    private String projects;

    /**
     * 教育经历列表（JSON格式）
     */
    @Schema(description = "教育经历列表（JSON格式）")
    private String education;

    /**
     * 获奖情况列表（JSON格式）
     */
    @Schema(description = "获奖情况列表（JSON格式）")
    private String awards;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
