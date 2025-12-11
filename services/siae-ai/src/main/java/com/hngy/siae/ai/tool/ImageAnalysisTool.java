package com.hngy.siae.ai.tool;

import com.hngy.siae.api.media.client.MediaFeignClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Component;

import java.util.Base64;

/**
 * 图片分析工具
 * <p>
 * 使用多模态模型分析图片内容
 * 支持图片识别、OCR、场景理解等功能
 *
 * @author SIAE Team
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ImageAnalysisTool {

    private final MediaFeignClient mediaFeignClient;
    private final ChatModel chatModel;

    /**
     * 获取图片的 Base64 编码
     * 供模型分析使用
     * 
     * @param fileId 文件ID（从媒体服务获取）
     * @return 图片的 Base64 编码字符串
     */
    @Tool(description = "获取图片的Base64编码数据，用于图片识别、OCR文字识别、场景理解等。提供文件ID即可。")
    public String getImageData(
            @ToolParam(description = "图片文件ID") String fileId) {
        
        log.info("Tool invoked: getImageData - fileId: {}", fileId);
        
        if (fileId == null || fileId.trim().isEmpty()) {
            throw ToolExecutionException.of("文件ID不能为空");
        }
        
        try {
            // 从媒体服务获取图片字节数据
            log.debug("Fetching image bytes from media service: fileId={}", fileId);
            byte[] imageBytes = mediaFeignClient.getFileBytes(fileId);
            
            if (imageBytes == null || imageBytes.length == 0) {
                throw ToolExecutionException.of("无法获取图片数据，文件可能不存在或为空");
            }
            
            log.info("Retrieved image bytes: fileId={}, size={} bytes", fileId, imageBytes.length);
            
            // 转为 Base64
            String base64 = Base64.getEncoder().encodeToString(imageBytes);
            
            log.info("Image data retrieved successfully: fileId={}, base64 length={}", fileId, base64.length());
            
            return "图片数据已获取（Base64编码，长度: " + base64.length() + " 字符）。" +
                   "注意：当前模型可能不支持直接处理图片，建议使用支持视觉功能的模型（如 llava、gpt-4-vision 等）。";
            
        } catch (Exception e) {
            log.error("Error getting image data: fileId={}", fileId, e);
            throw ToolExecutionException.of("获取图片数据失败: " + e.getMessage(), e);
        }
    }
}
