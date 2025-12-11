package com.hngy.siae.api.media.client;

import com.hngy.siae.api.media.dto.request.BatchUrlDTO;
import com.hngy.siae.api.media.dto.response.BatchDeleteVO;
import com.hngy.siae.api.media.dto.response.BatchUrlVO;
import com.hngy.siae.api.media.dto.response.FileInfoVO;
import com.hngy.siae.api.media.fallback.MediaFeignClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

/**
 * 媒体服务Feign客户端
 * 提供文件查询和URL获取相关的远程调用接口
 *
 * @author SIAE Team
 */
@FeignClient(
    name = "siae-media",
    path = "/api/v1/media/feign",
    contextId = "mediaFeignClient",
    fallback = MediaFeignClientFallback.class
)
public interface MediaFeignClient {
    
    /**
     * 根据文件ID获取文件详情
     *
     * @param fileId 文件ID
     * @return 文件详情
     */
    @GetMapping("/files/{fileId}")
    FileInfoVO getFileById(@NotBlank @PathVariable("fileId") String fileId);
    
    /**
     * 获取单个文件访问URL
     *
     * @param fileId 文件ID
     * @param expirySeconds URL过期时间（秒），默认24小时
     * @return 文件访问URL
     */
    @GetMapping("/files/{fileId}/url")
    String getFileUrl(
            @NotBlank @PathVariable("fileId") String fileId,
            @RequestParam(defaultValue = "86400") Integer expirySeconds
    );
    
    /**
     * 批量获取文件访问URL
     *
     * @param request 批量URL请求参数
     * @return 文件ID到URL的映射
     */
    @PostMapping("/files/urls/batch")
    BatchUrlVO batchGetFileUrls(@Valid @RequestBody BatchUrlDTO request);

    /**
     * 删除单个文件
     *
     * @param fileId 文件ID
     */
    @DeleteMapping("/files/{fileId}")
    void deleteFile(@NotBlank @PathVariable("fileId") String fileId);

    /**
     * 批量删除文件
     * 用于删除关联的媒体文件（如删除内容时清理关联的视频、图片等）
     * 注意：使用 POST 方法而非 DELETE，因为 DELETE 方法带 RequestBody 在某些环境下不被支持
     *
     * @param fileIds 文件ID列表
     * @return 批量删除结果
     */
    @PostMapping("/files/batch-delete")
    BatchDeleteVO batchDeleteFiles(@Valid @RequestBody java.util.List<@NotBlank String> fileIds);

    /**
     * 获取文件字节数据
     * 用于获取文件内容（如AI服务分析图片）
     *
     * @param fileId 文件ID
     * @return 文件字节数组
     */
    @GetMapping("/files/{fileId}/bytes")
    byte[] getFileBytes(@NotBlank @PathVariable("fileId") String fileId);
}
