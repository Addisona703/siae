package com.hngy.siae.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * AI服务配置属性
 * <p>
 * 支持配置LLM提供商、API密钥、模型参数等
 * 配置前缀: siae.ai
 * <p>
 * Requirements: 1.1, 1.4
 *
 * @author SIAE Team
 */
@Data
@Component
@Validated
@ConfigurationProperties(prefix = "siae.ai")
public class AiProperties {

    /**
     * LLM提供商
     * 支持: qwen (阿里通义千问), openai, ollama
     * 默认: qwen
     */
    private String provider = "qwen";

    /**
     * API密钥
     * 用于LLM提供商认证
     */
    private String apiKey;

    /**
     * 模型名称
     * 例如: qwen-turbo, qwen-plus, gpt-3.5-turbo, gpt-4
     * 默认: qwen-turbo
     */
    private String model = "qwen-turbo";

    /**
     * 可用模型列表
     * 用于前端展示可选模型
     */
    private java.util.List<String> availableModels = new java.util.ArrayList<>();

    /**
     * 支持视觉（图片）的模型列表
     * 只有这些模型可以处理图片输入
     */
    private java.util.List<String> visionModels = java.util.List.of(
            "gemma3", "llava", "llava:7b", "llava:13b", "llava:34b",
            "llava-llama3", "bakllava", "moondream", "minicpm-v"
    );

    /**
     * API基础URL
     * 用于自定义API端点，支持私有部署或代理
     */
    private String baseUrl;

    /**
     * 最大生成token数
     * 控制AI响应的最大长度
     * 默认: 2000
     */
    @Min(value = 100, message = "maxTokens最小值为100")
    @Max(value = 8000, message = "maxTokens最大值为8000")
    private Integer maxTokens = 2000;

    /**
     * 温度参数
     * 控制生成文本的随机性，范围0.0-2.0
     * 较低值产生更确定的输出，较高值产生更多样化的输出
     * 默认: 0.7
     */
    @Min(value = 0, message = "temperature最小值为0")
    @Max(value = 2, message = "temperature最大值为2")
    private Double temperature = 0.7;

    /**
     * 系统提示词
     * 用于设置AI的角色和行为
     */
    private String systemPrompt = "你是SIAE（软件协会）的智能助手。我可以帮助你查询以下信息：\n" +
            "1. 成员信息查询：按姓名、部门、职位查询成员详细信息\n" +
            "2. 获奖记录查询：查询指定成员的获奖记录\n" +
            "3. 统计数据：获取成员统计和获奖统计信息\n" +
            "请用简洁、专业的中文回答问题。当需要查询数据时，我会自动调用相应的工具函数获取最新信息。";

    /**
     * 响应超时时间（秒）
     * 默认: 30秒
     */
    @Min(value = 5, message = "responseTimeout最小值为5秒")
    @Max(value = 120, message = "responseTimeout最大值为120秒")
    private Integer responseTimeout = 30;

    /**
     * 会话配置
     */
    private Session session = new Session();

    /**
     * 重试配置
     */
    private Retry retry = new Retry();

    /**
     * 会话相关配置
     */
    @Data
    public static class Session {

        /**
         * 会话中保留的最大消息数
         * 超过此数量时，较早的消息将被移除
         * 默认: 20
         */
        @Min(value = 5, message = "maxMessages最小值为5")
        @Max(value = 100, message = "maxMessages最大值为100")
        private Integer maxMessages = 20;

        /**
         * 会话超时时间（分钟）
         * 超过此时间未活动的会话将被清理
         * 默认: 30分钟
         */
        @Min(value = 5, message = "timeoutMinutes最小值为5分钟")
        @Max(value = 1440, message = "timeoutMinutes最大值为1440分钟（24小时）")
        private Integer timeoutMinutes = 30;
    }

    /**
     * 重试相关配置
     */
    @Data
    public static class Retry {
        /**
         * 最大重试次数
         * 默认: 3
         */
        @Min(value = 1, message = "maxAttempts最小值为1")
        @Max(value = 10, message = "maxAttempts最大值为10")
        private Integer maxAttempts = 3;

        /**
         * 初始重试间隔（毫秒）
         * 默认: 1000ms
         */
        @Min(value = 100, message = "initialInterval最小值为100ms")
        private Long initialInterval = 1000L;

        /**
         * 重试间隔倍数
         * 默认: 2.0
         */
        @Min(value = 1, message = "multiplier最小值为1")
        private Double multiplier = 2.0;

        /**
         * 最大重试间隔（毫秒）
         * 默认: 10000ms
         */
        @Max(value = 60000, message = "maxInterval最大值为60000ms")
        private Long maxInterval = 10000L;
    }

    /**
     * 检查是否使用Qwen提供商
     */
    public boolean isQwenProvider() {
        return "qwen".equalsIgnoreCase(provider);
    }

    /**
     * 检查是否使用OpenAI提供商
     */
    public boolean isOpenAiProvider() {
        return "openai".equalsIgnoreCase(provider);
    }

    /**
     * 检查是否使用Ollama提供商
     */
    public boolean isOllamaProvider() {
        return "ollama".equalsIgnoreCase(provider);
    }

    /**
     * 检查指定模型是否支持视觉（图片）
     * @param modelName 模型名称
     * @return 是否支持视觉
     */
    public boolean isVisionModel(String modelName) {
        if (modelName == null || modelName.isBlank()) {
            return false;
        }
        // 检查模型名称是否以视觉模型开头（支持带版本号的情况，如 gemma3:4b）
        String lowerModel = modelName.toLowerCase();
        return visionModels.stream()
                .anyMatch(vm -> lowerModel.startsWith(vm.toLowerCase()));
    }

    /**
     * 获取有效的基础URL
     * 如果未配置，根据提供商返回默认URL
     */
    public String getEffectiveBaseUrl() {
        if (baseUrl != null && !baseUrl.isBlank()) {
            return baseUrl;
        }
        // 根据提供商返回默认URL
        if (isQwenProvider()) {
            return "https://dashscope.aliyuncs.com/compatible-mode/v1";
        } else if (isOpenAiProvider()) {
            return "https://api.openai.com/v1";
        } else if (isOllamaProvider()) {
            return "http://localhost:11434";
        }
        return null;
    }
}
