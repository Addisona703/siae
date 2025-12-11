package com.hngy.siae.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 更新用户简历 DTO
 *
 * @author KEYKB
 */
@Data
@Schema(description = "更新用户简历请求")
public class UserResumeUpdateDTO {

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
}
