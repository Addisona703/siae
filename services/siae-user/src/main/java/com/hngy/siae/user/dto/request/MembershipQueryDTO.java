package com.hngy.siae.user.dto.request;

import lombok.Data;

import java.time.LocalDate;

/**
 * 成员查询条件 DTO
 *
 * @author KEYKB
 */
@Data
public class MembershipQueryDTO {

    /**
     * 成员ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 生命周期状态：0候选，1正式，null查询所有
     */
    private Integer lifecycleStatus;

    /**
     * 部门ID（查询该部门的成员）
     */
    private Long departmentId;

    /**
     * 职位ID（查询担任该职位的成员）
     */
    private Long positionId;

    /**
     * 加入日期开始
     */
    private LocalDate joinDateStart;

    /**
     * 加入日期结束
     */
    private LocalDate joinDateEnd;

    /**
     * 用户名（模糊查询）
     */
    private String username;

    /**
     * 真实姓名（模糊查询）
     */
    private String realName;

    /**
     * 学号
     */
    private String studentId;

    /**
     * 关键字（统一搜索用户名、真实姓名、学号）
     */
    private String keyword;

    /**
     * 是否为现届成员：true查询现届（在读），false查询往届（离校），null查询所有
     */
    private Boolean isCurrentMember;

    /**
     * 年级（入学年份）
     */
    private Integer entryYear;

    /**
     * 是否排除待审核成员（内部使用，不对外暴露）
     * 当 lifecycleStatus 为 null 时，默认设置为 true
     */
    private Boolean excludePending;
}
