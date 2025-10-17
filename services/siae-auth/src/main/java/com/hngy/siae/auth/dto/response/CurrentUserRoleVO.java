package com.hngy.siae.auth.dto.response;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 当前用户角色视图对象
 *
 * @author KEYKB
 */
@Data
public class CurrentUserRoleVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 角色ID
     */
    private Long id;

    /**
     * 角色名称
     */
    private String name;

    /**
     * 角色编码
     */
    private String code;

    /**
     * 角色描述
     */
    private String description;
}
