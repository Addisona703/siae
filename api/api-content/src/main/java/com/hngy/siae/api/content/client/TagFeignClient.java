package com.hngy.siae.api.content.client;

import com.hngy.siae.api.content.dto.response.TagVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import jakarta.validation.constraints.NotNull;

/**
 * 标签服务Feign客户端
 * 提供标签查询相关的远程调用接口
 * 
 * 注意：需要在 Provider 服务中添加对应的 GET /{id} 接口
 *
 * @author KEYKB
 */
@FeignClient(
    name = "siae-content",
    path = "/tags",
    contextId = "tagFeignClient"
)
public interface TagFeignClient {
    
    /**
     * 根据ID查询标签
     * 注意：需要在 Provider 服务中添加此接口
     *
     * @param id 标签ID
     * @return 标签详情
     */
    @GetMapping("/{id}")
    TagVO getTagById(@NotNull @PathVariable("id") Long id);
}
