package com.hngy.siae.notification.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.hngy.siae.notification.enums.SendStatus;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 邮件发送记录实体类
 *
 * @author KEYKB
 */
@Data
@TableName("email_log")
public class EmailLog {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 收件人邮箱
     */
    private String recipient;

    /**
     * 邮件主题
     */
    private String subject;

    /**
     * 邮件内容
     */
    private String content;

    /**
     * 发送状态：0=待发送,1=成功,2=失败
     */
    private SendStatus status;

    /**
     * 错误信息
     */
    private String errorMsg;

    /**
     * 发送时间
     */
    private LocalDateTime sendTime;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
