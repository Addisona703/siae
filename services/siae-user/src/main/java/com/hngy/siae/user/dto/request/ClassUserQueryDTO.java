package com.hngy.siae.user.dto.request;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 班级用户关联查询数据传输对象
 * <p>
 * 用于班级用户关联分页查询和条件查询的数据传输，包含查询条件字段。
 * 所有字段都是可选的，不需要校验注解，因为查询条件通常允许为空。
 *
 * @author KEYKB
 */
@Data
public class ClassUserQueryDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 关联记录ID
     */
    private Long id;

    /**
     * 班级ID（通常为必填条件）
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
     * 用户名（用于模糊查询）
     */
    private String username;

    /**
     * 真实姓名（用于模糊查询）
     */
    private String realName;
}
