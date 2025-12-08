package com.hngy.siae.api.content.client;

import com.hngy.siae.api.content.dto.response.ContentDetailVO;
import com.hngy.siae.api.content.dto.response.ContentVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import jakarta.validation.constraints.NotNull;

/**
 * 内容服务Feign客户端
 * 提供内容查询相关的远程调用接口
 *
 * @author KEYKB
 */
@FeignClient(
    name = "siae-content",
    path = "/api/v1/content/feign",
    contextId = "contentFeignClient"
)
public interface ContentFeignClient {
    
    /**
     * 查询内容详情
     *
     * @param contentId 内容ID
     * @return 内容详情
     */
    @GetMapping("/content/{contentId}")
    ContentVO<ContentDetailVO> queryContent(@NotNull @PathVariable("contentId") Long contentId);
}
