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
 * 候选成员表实体类
 *
 * @author KEYKB
 */
@Data
@TableName("member_candidate")
public class MemberCandidate implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 候选成员ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 关联用户ID
     */
    private Long userId;

    /**
     * 学号
     */
    private String studentId;

    /**
     * 关联意向部门ID
     */
    private Long departmentId;

    /**
     * 状态：0待审核，1通过，2拒绝
     */
    private Integer status;

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