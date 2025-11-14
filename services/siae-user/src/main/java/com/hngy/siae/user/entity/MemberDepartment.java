package com.hngy.siae.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * 成员部门关联实体
 *
 * @author KEYKB
 */
@Data
@TableName("member_department")
public class MemberDepartment implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 成员ID（关联 membership 表）
     */
    private Long membershipId;

    /**
     * 部门ID
     */
    private Long departmentId;

    /**
     * 加入日期
     */
    private LocalDate joinDate;

    /**
     * 是否在该部门担任职位：0否，1是
     */
    private Integer hasPosition;
}
