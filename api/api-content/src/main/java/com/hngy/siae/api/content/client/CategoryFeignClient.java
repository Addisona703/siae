package com.hngy.siae.api.content.client;

import com.hngy.siae.api.content.dto.response.CategoryVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import jakarta.validation.constraints.NotNull;

/**
 * 分类服务Feign客户端
 * 提供分类查询相关的远程调用接口
 *
 * @author KEYKB
 */
@FeignClient(
    name = "siae-content",
    path = "/categories",
    contextId = "categoryFeignClient"
)
public interface CategoryFeignClient {
    
    /**
     * 查询分类详情
     *
     * @param categoryId 分类ID
     * @return 分类详情
     */
    @GetMapping("/detail/{categoryId}")
    CategoryVO queryCategory(@NotNull @PathVariable("categoryId") long categoryId);
}
