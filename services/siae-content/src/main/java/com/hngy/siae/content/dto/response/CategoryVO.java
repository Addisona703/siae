package com.hngy.siae.content.dto.response;

import java.util.Date;


import com.hngy.siae.content.enums.status.CategoryStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryVO {
    private Long id;
    private String name;
    private String code;
    private Long parentId;
    private CategoryStatusEnum status;
    private Date createTime;
    private Date updateTime;
}
