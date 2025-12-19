package com.hngy.siae.ai.service;

import com.hngy.siae.ai.domain.dto.ImageGenerationRequest;
import com.hngy.siae.ai.domain.dto.VideoGenerationRequest;
import com.hngy.siae.ai.domain.vo.ImageGenerationVO;
import com.hngy.siae.ai.domain.vo.VideoGenerationVO;
import reactor.core.publisher.Mono;

/**
 * 媒体生成服务接口
 * 支持图片生成和视频生成
 */
public interface MediaGenerationService {

    /**
     * 生成图片
     */
    Mono<ImageGenerationVO> generateImage(ImageGenerationRequest request, Long userId);

    /**
     * 生成视频（异步，返回任务ID）
     */
    Mono<VideoGenerationVO> generateVideo(VideoGenerationRequest request, Long userId);

    /**
     * 查询视频生成结果
     */
    Mono<VideoGenerationVO> getVideoResult(String taskId, Long userId);
}
