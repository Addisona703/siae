package com.hngy.siae.auth.config;

/**
 * CORS跨域配置
 * 
 * 注意：CORS 已在 Gateway 层统一配置，此处不再重复配置
 * 避免 Gateway 和微服务都添加 CORS 头导致重复的问题
 * 
 * @author SIAE
 */
// @Configuration  // 已禁用，由 Gateway 统一处理 CORS
public class CorsConfig {
    // CORS 配置已移至 siae-gateway 模块
    // 如需本地单独调试 auth 服务，可取消注释 @Configuration
}
