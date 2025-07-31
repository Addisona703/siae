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
 * 班级与用户关联表实体类
 *
 * @author KEYKB
 */
@Data
@TableName("class_user")
public class ClassUser implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 班级ID，关联ClassInfo实体
     */
    private Long classId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 成员类型：0非协会成员，1预备成员，2正式成员
     */
    private Integer memberType;

    /**
     * 状态：1在读，2转班，3毕业
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