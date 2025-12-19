package com.hngy.siae.ai.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 供应商信息 VO
 * <p>
 * 用于返回给前端的供应商和模型信息。
 * <p>
 * Requirements: 2.1
 *
 * @author SIAE Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProviderInfo {

    /**
     * 供应商名称（标识符）
     * 如 "zhipu"、"openai"
     */
    private String name;

    /**
     * 供应商显示名称
     * 如 "智谱AI"、"OpenAI"
     */
    private String displayName;

    /**
     * 可用模型列表
     */
    private List<String> models;

    /**
     * 默认模型
     */
    private String defaultModel;

    /**
     * 供应商是否可用
     */
    private boolean available;
}
