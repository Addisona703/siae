package com.hngy.siae.api.ai.client;

import com.hngy.siae.api.ai.dto.response.ContentInfo;
import com.hngy.siae.api.ai.fallback.AiContentFeignClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * AI服务内容数据 Feign 客户端
 * <p>
 * 提供AI服务所需的内容数据查询接口。
 *
 * @author SIAE Team
 */
@FeignClient(
    name = "siae-content",
    path = "/api/v1/content/feign/ai",
    contextId = "aiContentFeignClient",
    fallback = AiContentFeignClientFallback.class
)
public interface AiContentFeignClient {
    
    /**
     * 搜索内容
     *
     * @param keyword 关键词
     * @param categoryName 分类名称，可选
     * @param limit 返回数量限制，默认10
     * @return 内容信息列表
     */
    @GetMapping("/search")
    List<ContentInfo> searchContent(
        @RequestParam(value = "keyword", required = false) String keyword,
        @RequestParam(value = "categoryName", required = false) String categoryName,
        @RequestParam(value = "limit", defaultValue = "10") Integer limit
    );
    
    /**
     * 获取热门内容
     *
     * @param limit 返回数量限制，默认10
     * @return 热门内容列表
     */
    @GetMapping("/hot")
    List<ContentInfo> getHotContent(
        @RequestParam(value = "limit", defaultValue = "10") Integer limit
    );
    
    /**
     * 获取最新内容
     *
     * @param limit 返回数量限制，默认10
     * @return 最新内容列表
     */
    @GetMapping("/latest")
    List<ContentInfo> getLatestContent(
        @RequestParam(value = "limit", defaultValue = "10") Integer limit
    );
}
