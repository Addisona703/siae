package com.hngy.siae.user.dto.request;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * 正式成员查询数据传输对象
 * <p>
 * 用于正式成员分页查询和条件查询的数据传输，包含查询条件字段。
 * 所有字段都是可选的，不需要校验注解，因为查询条件通常允许为空。
 *
 * @author KEYKB
 */
@Data
public class MemberQueryDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 正式成员ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名（用于模糊查询）
     */
    private String username;

    /**
     * 真实姓名（用于模糊查询）
     */
    private String realName;

    /**
     * 学号
     */
    private String studentId;

    /**
     * 部门ID
     */
    private Long departmentId;

    /**
     * 职位ID
     */
    private Long positionId;

    /**
     * 状态：1在校，2离校，3毕业
     */
    private Integer status;

    /**
     * 入会日期范围-开始
     */
    private LocalDate joinDateStart;

    /**
     * 入会日期范围-结束
     */
    private LocalDate joinDateEnd;
}
