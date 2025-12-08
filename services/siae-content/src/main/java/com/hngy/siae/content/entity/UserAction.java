package com.hngy.siae.content.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.*;
import com.hngy.siae.content.enums.ActionTypeEnum;
import com.hngy.siae.content.enums.TypeEnum;
import com.hngy.siae.content.enums.status.ActionStatusEnum;
import lombok.Data;

/**
 * 内容行为表
 * @TableName user_action
 */
@Data
@TableName("user_action")
public class UserAction {
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 内容ID
     */
    private Long targetId;

    /**
     * 操作的类型
     */
    private TypeEnum targetType;

    /**
     * 操作类型：like、favorite、view
     */
    private ActionTypeEnum actionType;

    /**
     * 状态：0取消，1激活
     */
    private ActionStatusEnum status;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}