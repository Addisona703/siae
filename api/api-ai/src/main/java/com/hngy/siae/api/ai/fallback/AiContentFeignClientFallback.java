package com.hngy.siae.api.ai.fallback;

import com.hngy.siae.api.ai.client.AiContentFeignClient;
import com.hngy.siae.api.ai.dto.response.ContentInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * AI内容服务Feign客户端降级处理
 *
 * @author SIAE Team
 */
@Slf4j
@Component
public class AiContentFeignClientFallback implements AiContentFeignClient {
    
    @Override
    public List<ContentInfo> searchContent(String keyword, String categoryName, Integer limit) {
        log.warn("AiContentFeignClient.searchContent fallback triggered - keyword: {}", keyword);
        return Collections.emptyList();
    }
    
    @Override
    public List<ContentInfo> getHotContent(Integer limit) {
        log.warn("AiContentFeignClient.getHotContent fallback triggered");
        return Collections.emptyList();
    }
    
    @Override
    public List<ContentInfo> getLatestContent(Integer limit) {
        log.warn("AiContentFeignClient.getLatestContent fallback triggered");
        return Collections.emptyList();
    }
}
