package com.hngy.siae.api.ai.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * AI服务内容信息DTO
 * <p>
 * 用于AI工具返回的内容摘要信息
 *
 * @author SIAE Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /** 内容ID */
    private Long id;
    
    /** 标题 */
    private String title;
    
    /** 内容类型：article/video/image/file */
    private String type;
    
    /** 简介/描述 */
    private String description;
    
    /** 作者昵称 */
    private String authorNickname;
    
    /** 分类名称 */
    private String categoryName;
    
    /** 浏览量 */
    private Long viewCount;
    
    /** 点赞数 */
    private Long likeCount;
    
    /** 发布时间 */
    private LocalDateTime publishTime;
}
