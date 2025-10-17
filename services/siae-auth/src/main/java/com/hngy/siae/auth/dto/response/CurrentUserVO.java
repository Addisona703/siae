package com.hngy.siae.auth.dto.response;

import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 当前用户信息视图对象
 *
 * <p>封装当前登录用户的基础资料、角色及权限列表，提供给前端用于构建个性化界面和权限控制。</p>
 *
 * @author KEYKB
 */
@Data
@Builder
public class CurrentUserVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 用户邮箱
     */
    private String email;

    /**
     * 用户头像
     */
    private String avatar;

    /**
     * 用户角色列表
     */
    private List<String> roles;

    /**
     * 用户权限编码列表
     */
    private List<String> permissions;
}
