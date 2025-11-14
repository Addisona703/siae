package com.hngy.siae.user.dto.response;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 部门VO
 *
 * @author KEYKB
 */
@Data
public class DepartmentVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 部门ID
     */
    private Long id;

    /**
     * 部门名称
     */
    private String name;
}
