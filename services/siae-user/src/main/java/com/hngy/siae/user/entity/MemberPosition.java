package com.hngy.siae.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * 成员职位历史实体
 * 原 user_position 表重命名为 member_position
 * 关联字段从 userId 改为 membershipId
 *
 * @author KEYKB
 */
@Data
@TableName("member_position")
public class MemberPosition implements Serializable {

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
     * 职位ID
     */
    private Long positionId;

    /**
     * 部门ID，NULL 表示全协会职位
     */
    private Long departmentId;

    /**
     * 任职开始日期
     */
    private LocalDate startDate;

    /**
     * 任职结束日期，NULL 表示仍在任
     */
    private LocalDate endDate;
}
