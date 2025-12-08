package com.hngy.siae.api.content.fallback;

import com.hngy.siae.api.content.client.TagFeignClient;
import com.hngy.siae.api.content.dto.response.TagVO;
import com.hngy.siae.core.exception.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * TagFeignClient 降级实现
 * 当标签服务不可用时执行降级逻辑
 *
 * @author KEYKB
 */
@Component
@Slf4j
public class TagFeignClientFallback implements TagFeignClient {
    
    @Override
    public TagVO getTagById(Long id) {
        log.error("查询标签服务不可用，tagId: {}", id);
        throw new ServiceException(503, "标签服务暂时不可用，请稍后重试");
    }
}
