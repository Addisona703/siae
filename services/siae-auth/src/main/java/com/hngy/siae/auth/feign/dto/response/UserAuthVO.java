package com.hngy.siae.auth.feign.dto.response;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户认证信息视图对象
 * <p>
 * 专门用于内部服务间调用（如认证服务），包含密码等敏感信息。
 * <strong>警告：此DTO包含敏感信息，仅限内部Feign调用使用，禁止用于对外API！</strong>
 *
 * @author KEYKB
 */
@Data
public class UserAuthVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码（加密后的密码）
     * <p>
     * <strong>安全提示：</strong>此字段包含用户的加密密码，
     * 仅用于内部服务间的身份验证，请确保传输安全。
     */
    private String password;

    /**
     * 学号
     */
    private String studentId;

    /**
     * 用户头像文件ID
     */
    private String avatarFileId;

    /**
     * 状态：0禁用，1启用
     */
    private Integer status;
}
