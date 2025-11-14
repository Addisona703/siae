package com.hngy.siae.notification.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.hngy.siae.notification.enums.SendStatus;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 短信发送记录实体类
 *
 * @author KEYKB
 */
@Data
@TableName("sms_log")
public class SmsLog {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 短信内容
     */
    private String content;

    /**
     * 模板代码
     */
    private String templateCode;

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
