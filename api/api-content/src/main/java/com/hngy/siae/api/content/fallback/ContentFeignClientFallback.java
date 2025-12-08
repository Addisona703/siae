package com.hngy.siae.api.content.fallback;

import com.hngy.siae.api.content.client.ContentFeignClient;
import com.hngy.siae.api.content.dto.response.ContentDetailVO;
import com.hngy.siae.api.content.dto.response.ContentVO;
import com.hngy.siae.core.exception.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * ContentFeignClient 降级实现
 * 当内容服务不可用时执行降级逻辑
 *
 * @author KEYKB
 */
@Component
@Slf4j
public class ContentFeignClientFallback implements ContentFeignClient {
    
    @Override
    public ContentVO<ContentDetailVO> queryContent(Long contentId) {
        log.error("查询内容服务不可用，contentId: {}", contentId);
        throw new ServiceException(503, "内容服务暂时不可用，请稍后重试");
    }
}
