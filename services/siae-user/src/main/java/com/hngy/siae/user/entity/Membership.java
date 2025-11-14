package com.hngy.siae.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hngy.siae.user.enums.LifecycleStatusEnum;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 成员统一表实体类
 * 合并了原 member 和 member_candidate 表
 * 通过 lifecycleStatus 字段区分候选成员(0)和正式成员(1)
 *
 * @author KEYKB
 */
@Data
@TableName("membership")
public class Membership implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 记录ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID（唯一）
     */
    private Long userId;

    /**
     * 成员大头照文件ID
     */
    private String headshotFileId;

    /**
     * 生命周期状态：0候选，1正式
     */
    private LifecycleStatusEnum lifecycleStatus;

    /**
     * 成为正式成员的日期（候选时为NULL）
     */
    private LocalDate joinDate;

    /**
     * 是否删除：0否，1是
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
