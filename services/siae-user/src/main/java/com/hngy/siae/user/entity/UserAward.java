package com.hngy.siae.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户获奖记录表实体类
 *
 * @author KEYKB
 */
@Data
@TableName("user_award")
public class UserAward implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键，自增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 奖项名称
     */
    private String awardTitle;

    /**
     * 关联奖项等级ID
     */
    private Long awardLevelId;

    /**
     * 关联奖项类型ID
     */
    private Long awardTypeId;

    /**
     * 颁发单位
     */
    private String awardedBy;

    /**
     * 获奖时间
     */
    private LocalDate awardedAt;

    /**
     * 获奖描述（选填）
     */
    private String description;

    /**
     * 奖状或证明材料的URL
     */
    private String certificateUrl;

    /**
     * 团队成员信息
     */
    private String teamMembers;

    /**
     * 是否删除：0否，1是
     */
    @TableLogic
    private Integer isDeleted;

    /**
     * 记录创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 记录更新时间
     */
    private LocalDateTime updatedAt;
} 