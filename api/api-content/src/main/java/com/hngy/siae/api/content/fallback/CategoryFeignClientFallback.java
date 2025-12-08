package com.hngy.siae.api.content.fallback;

import com.hngy.siae.api.content.client.CategoryFeignClient;
import com.hngy.siae.api.content.dto.response.CategoryVO;
import com.hngy.siae.core.exception.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * CategoryFeignClient 降级实现
 * 当分类服务不可用时执行降级逻辑
 *
 * @author KEYKB
 */
@Component
@Slf4j
public class CategoryFeignClientFallback implements CategoryFeignClient {
    
    @Override
    public CategoryVO queryCategory(long categoryId) {
        log.error("查询分类服务不可用，categoryId: {}", categoryId);
        throw new ServiceException(503, "分类服务暂时不可用，请稍后重试");
    }
}
