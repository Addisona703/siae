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
 * 用户主表实体类
 * 存储用户最核心的登录认证信息
 *
 * @author KEYKB
 */
@Data
@TableName("user")
public class User implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键，使用雪花算法生成
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 登录名/用户名
     */
    private String username;

    /**
     * 加密密码 (使用BCrypt)
     */
    private String password;

    /**
     * 状态：0禁用，1启用
     */
    private Integer status;

    /**
     * 是否逻辑删除：0否，1是
     */
    @TableLogic
    private Integer isDeleted = 0;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
} 