package com.hngy.siae.user.feign;

import com.hngy.siae.core.result.Result;
import com.hngy.siae.user.feign.dto.BatchUrlRequest;
import com.hngy.siae.user.feign.dto.BatchUrlResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Media服务Feign客户端
 *
 * @author SIAE Team
 */
@FeignClient(
        name = "siae-media",
        url = "${feign.media.url:http://localhost:8040}",
        path = "/api/v1/media/files"
)
public interface MediaFeignClient {

    /**
     * 获取单个文件访问URL
     *
     * @param fileId 文件ID
     * @param expirySeconds URL过期时间（秒）
     * @return 文件访问URL
     */
    @GetMapping("/{fileId}/url")
    Result<String> getFileUrl(
            @PathVariable("fileId") String fileId,
            @RequestParam(value = "expirySeconds", defaultValue = "86400") Integer expirySeconds
    );

    /**
     * 批量获取文件访问URL
     *
     * @param request 批量URL请求
     * @return 批量URL响应
     */
    @PostMapping("/urls/batch")
    Result<BatchUrlResponse> batchGetFileUrls(@RequestBody BatchUrlRequest request);

}
