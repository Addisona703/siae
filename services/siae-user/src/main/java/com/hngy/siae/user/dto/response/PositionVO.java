package com.hngy.siae.user.dto.response;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 职位VO
 *
 * @author KEYKB
 */
@Data
public class PositionVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 职位ID
     */
    private Long id;

    /**
     * 职位名称
     */
    private String name;
}
