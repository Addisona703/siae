package com.hngy.siae.ai.tool;

import com.hngy.siae.api.ai.client.AiContentFeignClient;
import com.hngy.siae.api.ai.dto.response.ContentInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * 内容查询工具
 * <p>
 * 提供AI可调用的内容数据查询功能
 *
 * @author SIAE Team
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ContentQueryTool {

    private final AiContentFeignClient aiContentFeignClient;
    private final com.hngy.siae.ai.security.PermissionChecker permissionChecker;

    /**
     * 搜索内容
     */
    @Tool(description = "搜索内容，可按关键词、分类搜索文章、视频、图片等内容。返回内容的标题、描述、作者、分类、浏览量等信息。")
    public List<ContentInfo> searchContent(
            @ToolParam(description = "搜索关键词") String keyword,
            @ToolParam(description = "分类名称，可选，如：技术分享、活动通知、学习资料") String categoryName,
            @ToolParam(description = "返回数量限制，默认10，最大20") Integer limit) {
        
        log.info("Tool invoked: searchContent - keyword: {}, category: {}, user: {}", 
                keyword, categoryName, permissionChecker.getCurrentUsername());
        
        // 内容搜索不需要登录，公开内容可以匿名访问
        
        try {
            int effectiveLimit = (limit == null || limit <= 0) ? 10 : Math.min(limit, 20);
            List<ContentInfo> contents = aiContentFeignClient.searchContent(keyword, categoryName, effectiveLimit);
            
            log.info("searchContent returned {} items", contents != null ? contents.size() : 0);
            return contents != null ? contents : Collections.emptyList();
        } catch (Exception e) {
            log.error("Error searching content: {}", e.getMessage(), e);
            throw ToolExecutionException.of("搜索内容失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取热门内容
     */
    @Tool(description = "获取热门内容，返回浏览量最高的内容列表。")
    public List<ContentInfo> getHotContent(
            @ToolParam(description = "返回数量限制，默认10，最大20") Integer limit) {
        
        log.info("Tool invoked: getHotContent - limit: {}, user: {}", 
                limit, permissionChecker.getCurrentUsername());
        
        try {
            int effectiveLimit = (limit == null || limit <= 0) ? 10 : Math.min(limit, 20);
            List<ContentInfo> contents = aiContentFeignClient.getHotContent(effectiveLimit);
            
            log.info("getHotContent returned {} items", contents != null ? contents.size() : 0);
            return contents != null ? contents : Collections.emptyList();
        } catch (Exception e) {
            log.error("Error getting hot content: {}", e.getMessage(), e);
            throw ToolExecutionException.of("获取热门内容失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取最新内容
     */
    @Tool(description = "获取最新发布的内容，返回最近发布的内容列表。")
    public List<ContentInfo> getLatestContent(
            @ToolParam(description = "返回数量限制，默认10，最大20") Integer limit) {
        
        log.info("Tool invoked: getLatestContent - limit: {}, user: {}", 
                limit, permissionChecker.getCurrentUsername());
        
        try {
            int effectiveLimit = (limit == null || limit <= 0) ? 10 : Math.min(limit, 20);
            List<ContentInfo> contents = aiContentFeignClient.getLatestContent(effectiveLimit);
            
            log.info("getLatestContent returned {} items", contents != null ? contents.size() : 0);
            return contents != null ? contents : Collections.emptyList();
        } catch (Exception e) {
            log.error("Error getting latest content: {}", e.getMessage(), e);
            throw ToolExecutionException.of("获取最新内容失败: " + e.getMessage(), e);
        }
    }
}
