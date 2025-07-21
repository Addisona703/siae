package com.hngy.siae.content.dto.response.detail;


import com.hngy.siae.content.dto.response.ContentDetailVO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 文章vo
 *
 * @author KEYKB
 * &#064;date: 2025/05/19
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ArticleVO implements ContentDetailVO {
    private Long id;
    private Long contentId;
    private String content;
    private String coverUrl;
    private Date createTime;
    private Date updateTime;
}