package com.hngy.siae.ai.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hngy.siae.ai.config.AiProviderProperties;
import com.hngy.siae.ai.domain.dto.ImageGenerationRequest;
import com.hngy.siae.ai.domain.dto.VideoGenerationRequest;
import com.hngy.siae.ai.domain.vo.ImageGenerationVO;
import com.hngy.siae.ai.domain.vo.VideoGenerationVO;
import com.hngy.siae.ai.exception.AiException;
import com.hngy.siae.ai.service.MediaGenerationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.*;

/**
 * 媒体生成服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MediaGenerationServiceImpl implements MediaGenerationService {

    private final AiProviderProperties aiProviderProperties;
    private final ObjectMapper objectMapper;

    private static final String IMAGES_GENERATIONS_PATH = "/images/generations";
    private static final String VIDEOS_GENERATIONS_PATH = "/videos/generations";
    private static final String ASYNC_RESULT_PATH = "/async-result/{id}";

    private WebClient getWebClient() {
        AiProviderProperties.ProviderConfig config = aiProviderProperties.getProviders().get("zhipu");
        if (config == null || !config.isValid()) {
            throw AiException.providerNotFound("zhipu");
        }
        return WebClient.builder()
                .baseUrl(config.getBaseUrl())
                .defaultHeader("Authorization", "Bearer " + config.getApiKey())
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Override
    public Mono<ImageGenerationVO> generateImage(ImageGenerationRequest request, Long userId) {
        log.info("Generating image for user: {}, prompt: {}", userId, request.getPrompt());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", request.getModel());
        body.put("prompt", request.getPrompt());
        if (request.getSize() != null) {
            body.put("size", request.getSize());
        }
        if (request.getN() != null) {
            body.put("n", request.getN());
        }

        return getWebClient().post()
                .uri(IMAGES_GENERATIONS_PATH)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(120))
                .map(this::parseImageResponse)
                .doOnSuccess(vo -> log.info("Image generated successfully for user: {}", userId))
                .doOnError(e -> log.error("Image generation failed for user: {}", userId, e));
    }

    @Override
    public Mono<VideoGenerationVO> generateVideo(VideoGenerationRequest request, Long userId) {
        log.info("Generating video for user: {}, prompt: {}", userId, request.getPrompt());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", request.getModel());
        body.put("prompt", request.getPrompt());
        if (request.getImageUrl() != null) {
            body.put("image_url", request.getImageUrl());
        }
        if (request.getSize() != null) {
            body.put("size", request.getSize());
        }
        if (request.getFps() != null) {
            body.put("fps", request.getFps());
        }
        if (request.getWithAudio() != null) {
            body.put("with_audio", request.getWithAudio());
        }
        if (request.getQuality() != null) {
            body.put("quality", request.getQuality());
        }
        if (request.getDuration() != null) {
            body.put("duration", request.getDuration());
        }

        return getWebClient().post()
                .uri(VIDEOS_GENERATIONS_PATH)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(60))
                .map(this::parseVideoSubmitResponse)
                .doOnSuccess(vo -> log.info("Video task submitted for user: {}, taskId: {}", userId, vo.getTaskId()))
                .doOnError(e -> log.error("Video generation failed for user: {}", userId, e));
    }

    @Override
    public Mono<VideoGenerationVO> getVideoResult(String taskId, Long userId) {
        log.info("Querying video result for user: {}, taskId: {}", userId, taskId);

        return getWebClient().get()
                .uri(uriBuilder -> uriBuilder.path("/async-result/{id}").build(taskId))
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(30))
                .map(this::parseVideoResultResponse)
                .doOnSuccess(vo -> log.info("Video result retrieved for taskId: {}, status: {}", taskId, vo.getTaskStatus()))
                .doOnError(e -> log.error("Failed to get video result for taskId: {}", taskId, e));
    }

    private ImageGenerationVO parseImageResponse(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            List<ImageGenerationVO.ImageData> images = new ArrayList<>();

            JsonNode dataNode = root.get("data");
            if (dataNode != null && dataNode.isArray()) {
                for (JsonNode item : dataNode) {
                    ImageGenerationVO.ImageData imageData = ImageGenerationVO.ImageData.builder()
                            .url(item.has("url") ? item.get("url").asText() : null)
                            .b64Json(item.has("b64_json") ? item.get("b64_json").asText() : null)
                            .build();
                    images.add(imageData);
                }
            }

            return ImageGenerationVO.builder()
                    .data(images)
                    .created(root.has("created") ? root.get("created").asLong() : System.currentTimeMillis() / 1000)
                    .build();
        } catch (Exception e) {
            log.error("Failed to parse image response: {}", response, e);
            throw AiException.llmProviderError(e);
        }
    }

    private VideoGenerationVO parseVideoSubmitResponse(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            return VideoGenerationVO.builder()
                    .taskId(root.has("id") ? root.get("id").asText() : null)
                    .taskStatus(root.has("task_status") ? root.get("task_status").asText() : "PROCESSING")
                    .build();
        } catch (Exception e) {
            log.error("Failed to parse video submit response: {}", response, e);
            throw AiException.llmProviderError(e);
        }
    }

    private VideoGenerationVO parseVideoResultResponse(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            String taskStatus = root.has("task_status") ? root.get("task_status").asText() : "PROCESSING";

            List<VideoGenerationVO.VideoData> videos = new ArrayList<>();
            JsonNode videoResultNode = root.get("video_result");
            if (videoResultNode != null && videoResultNode.isArray()) {
                for (JsonNode item : videoResultNode) {
                    VideoGenerationVO.VideoData videoData = VideoGenerationVO.VideoData.builder()
                            .url(item.has("url") ? item.get("url").asText() : null)
                            .coverImageUrl(item.has("cover_image_url") ? item.get("cover_image_url").asText() : null)
                            .build();
                    videos.add(videoData);
                }
            }

            return VideoGenerationVO.builder()
                    .taskId(root.has("id") ? root.get("id").asText() : null)
                    .taskStatus(taskStatus)
                    .videoResult(videos.isEmpty() ? null : videos)
                    .build();
        } catch (Exception e) {
            log.error("Failed to parse video result response: {}", response, e);
            throw AiException.llmProviderError(e);
        }
    }
}
