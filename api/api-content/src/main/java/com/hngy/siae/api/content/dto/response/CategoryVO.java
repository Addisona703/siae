package com.hngy.siae.api.content.dto.response;

import com.hngy.siae.api.content.enums.CategoryStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 分类VO
 *
 * @author KEYKB
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryVO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private String name;
    private String code;
    private Long parentId;
    private CategoryStatusEnum status;
    private Date createTime;
    private Date updateTime;
}
