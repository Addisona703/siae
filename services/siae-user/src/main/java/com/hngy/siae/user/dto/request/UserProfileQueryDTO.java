package com.hngy.siae.user.dto.request;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * 用户详情查询数据传输对象
 * <p>
 * 用于用户详情分页查询和条件查询的数据传输，包含查询条件字段。
 * 所有字段都是可选的，不需要校验注解，因为查询条件通常允许为空。
 *
 * @author KEYKB
 */
@Data
public class UserProfileQueryDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 昵称（支持模糊查询）
     */
    private String nickname;

    /**
     * 真实姓名（支持模糊查询）
     */
    private String realName;

    /**
     * 邮箱（支持模糊查询）
     */
    private String email;

    /**
     * 手机号码
     */
    private String phone;

    /**
     * QQ号
     */
    private String qq;

    /**
     * 微信号
     */
    private String wechat;

    /**
     * 身份证号
     */
    private String idCard;

    /**
     * 性别：0未知，1男，2女
     */
    private Integer gender;

    /**
     * 出生日期范围-开始
     */
    private LocalDate birthdayStart;

    /**
     * 出生日期范围-结束
     */
    private LocalDate birthdayEnd;
}
