package com.hngy.siae.auth.feign.dto.response;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户基本信息视图对象
 * <p>
 * 轻量级的用户信息视图对象，仅包含用户的基本认证信息。
 * 专门用于服务间内部调用，减少数据传输量。
 * <p>
 * <strong>安全警告：</strong>此VO包含用户密码字段，仅限内部服务调用使用，
 * 不得用于对外API接口，以防止敏感信息泄露。
 *
 * @author KEYKB
 */
@Data
public class UserBasicVO implements Serializable {

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
