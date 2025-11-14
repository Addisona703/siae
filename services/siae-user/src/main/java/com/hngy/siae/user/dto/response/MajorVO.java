package com.hngy.siae.user.dto.response;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 专业VO
 *
 * @author KEYKB
 */
@Data
public class MajorVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 专业ID
     */
    private Long id;

    /**
     * 专业名称
     */
    private String name;

    /**
     * 专业代码
     */
    private String code;

    /**
     * 专业简称
     */
    private String abbr;

    /**
     * 所属学院
     */
    private String collegeName;
}
