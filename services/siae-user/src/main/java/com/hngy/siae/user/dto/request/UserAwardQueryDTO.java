package com.hngy.siae.user.dto.request;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * 用户获奖记录查询数据传输对象
 * <p>
 * 用于用户获奖记录分页查询和条件查询的数据传输，包含查询条件字段。
 * 所有字段都是可选的，不需要校验注解，因为查询条件通常允许为空。
 *
 * @author KEYKB
 */
@Data
public class UserAwardQueryDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 获奖记录ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名（用于模糊查询）
     */
    private String username;

    /**
     * 真实姓名（用于模糊查询）
     */
    private String realName;

    /**
     * 奖项级别ID
     */
    private Long awardLevelId;

    /**
     * 奖项类型ID
     */
    private Long awardTypeId;

    /**
     * 奖项名称（用于模糊查询）
     */
    private String awardTitle;

    /**
     * 颁发单位（用于模糊查询）
     */
    private String awardedBy;

    /**
     * 获奖日期范围-开始
     */
    private LocalDate awardDateStart;

    /**
     * 获奖日期范围-结束
     */
    private LocalDate awardDateEnd;
}