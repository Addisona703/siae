package com.hngy.siae.content.controller;

import com.hngy.siae.api.ai.dto.response.ContentInfo;
import com.hngy.siae.core.result.Result;
import com.hngy.siae.content.service.ContentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * AI服务Feign控制器
 * <p>
 * 专门用于AI服务调用的REST API接口，提供内容查询功能。
 *
 * @author SIAE Team
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/feign/ai")
@Validated
@Tag(name = "AI服务Feign接口", description = "AI服务内容查询API")
public class AiFeignController {

    private final ContentService contentService;

    /**
     * 搜索内容
     */
    @GetMapping("/search")
    @Operation(summary = "搜索内容", description = "根据关键词和分类搜索内容")
    public Result<List<ContentInfo>> searchContent(
            @Parameter(description = "搜索关键词")
            @RequestParam(value = "keyword", required = false) String keyword,
            @Parameter(description = "分类名称")
            @RequestParam(value = "categoryName", required = false) String categoryName,
            @Parameter(description = "返回数量限制")
            @RequestParam(value = "limit", defaultValue = "10") Integer limit) {
        log.info("AI Feign调用: 搜索内容, keyword={}, categoryName={}, limit={}", keyword, categoryName, limit);
        return Result.success(contentService.searchForAi(keyword, categoryName, limit));
    }

    /**
     * 获取热门内容
     */
    @GetMapping("/hot")
    @Operation(summary = "获取热门内容", description = "获取浏览量最高的内容")
    public Result<List<ContentInfo>> getHotContent(
            @Parameter(description = "返回数量限制")
            @RequestParam(value = "limit", defaultValue = "10") Integer limit) {
        log.info("AI Feign调用: 获取热门内容, limit={}", limit);
        return Result.success(contentService.getHotContentForAi(limit));
    }

    /**
     * 获取最新内容
     */
    @GetMapping("/latest")
    @Operation(summary = "获取最新内容", description = "获取最近发布的内容")
    public Result<List<ContentInfo>> getLatestContent(
            @Parameter(description = "返回数量限制")
            @RequestParam(value = "limit", defaultValue = "10") Integer limit) {
        log.info("AI Feign调用: 获取最新内容, limit={}", limit);
        return Result.success(contentService.getLatestContentForAi(limit));
    }
}
