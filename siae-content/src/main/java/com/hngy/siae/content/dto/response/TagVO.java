package com.hngy.siae.content.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * <p></p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TagVO {
    private Long id;
    private String name;
    private String description;
    private Date createTime;
    private Date updateTime;
}
