package com.hngy.siae.user.dto.response;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 班级用户关联视图对象
 * <p>
 * 用于返回班级用户关联信息的数据传输对象，包含关联信息和用户基本信息。
 *
 * @author KEYKB
 */
@Data
public class ClassUserVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 关联记录ID
     */
    private Long id;

    /**
     * 班级ID
     */
    private Long classId;

    /**
     * 班级名称
     */
    private String className;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 成员类型：0非协会成员，1预备成员，2正式成员
     */
    private Integer memberType;

    /**
     * 成员类型名称
     */
    private String memberTypeName;

    /**
     * 状态：1在读，2转班，3毕业
     */
    private Integer status;

    /**
     * 状态名称
     */
    private String statusName;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
