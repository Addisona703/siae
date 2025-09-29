package com.hngy.siae.auth.filter;

import com.hngy.siae.core.utils.JwtUtils;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.springframework.stereotype.Component;

/**
 * Feign服务间调用拦截器
 * <p>
 * 自动为认证服务发起的Feign调用添加服务间调用token，
 * 确保服务间调用能够通过目标服务的安全验证。
 *
 * ⚠️ 已废弃：该拦截器已被 siae-web-starter 中的 FeignAuthenticationInterceptor 替代
 * 新版本使用轻量级内部认证机制，性能更优，架构更清晰
 *
 * @author SIAE开发团队
 * @deprecated 使用 com.hngy.siae.web.config.FeignAuthenticationInterceptor 替代
 */
@Slf4j
//@Component  // 注释掉，禁用此拦截器
@RequiredArgsConstructor
@Deprecated
public class ServiceCallFeignInterceptor implements RequestInterceptor {

    private final JwtUtils jwtUtils;

    @Override
    public void apply(RequestTemplate template) {
        // 该方法已被废弃，请使用 siae-web-starter 中的 FeignAuthenticationInterceptor
        log.warn("⚠️ ServiceCallFeignInterceptor 已废弃，请使用 FeignAuthenticationInterceptor");

        /*
        try {
            log.debug("=== Feign服务间调用拦截器开始处理 ===");

            // 获取请求信息用于日志记录
            String method = template.method();
            String url = template.url();

            log.debug("请求信息: {} {}", method, url);

            // 生成服务间调用专用token
            log.debug("开始生成服务间调用token...");
            String serviceCallToken;
            try {
                serviceCallToken = jwtUtils.createServiceCallToken();
                log.debug("✅ 服务间调用token生成成功: {}...", serviceCallToken.substring(0, Math.min(20, serviceCallToken.length())));
            } catch (Exception tokenException) {
                log.error("❌ 生成服务间调用token失败", tokenException);
                throw new RuntimeException("服务间调用token生成失败: " + tokenException.getMessage(), tokenException);
            }

            // 验证token是否有效
            if (serviceCallToken.trim().isEmpty()) {
                log.error("❌ 生成的服务间调用token为空");
                throw new RuntimeException("生成的服务间调用token为空");
            }

            // 添加Authorization头
            log.debug("设置Authorization头...");
            template.header(HttpHeaders.AUTHORIZATION, "Bearer " + serviceCallToken);

            // 添加服务间调用标识头
            log.debug("设置服务间调用标识头...");
            template.header("X-Service-Call", "true");
            template.header("X-Service-Name", "siae-auth");
            template.header("User-Agent", "siae-auth-service/1.0 (Feign)");

            // 记录设置的请求头（INFO级别，便于问题排查）
            log.info("=== Feign服务间调用配置完成 ===");
            log.info("目标服务: {} {}", method, url);
            log.info("完整token: {}", serviceCallToken);
            log.info("请求头配置:");
            log.info("  ✅ Authorization: Bearer {}", serviceCallToken);
            log.info("  ✅ X-Service-Call: true");
            log.info("  ✅ X-Service-Name: siae-auth");
            log.info("  ✅ User-Agent: siae-auth-service/1.0 (Feign)");

            log.debug("=== Feign服务间调用拦截器处理完成 ===");

        } catch (RuntimeException e) {
            // 重新抛出运行时异常
            throw e;
        } catch (Exception e) {
            log.error("❌ 配置Feign服务间调用时发生未知异常", e);
            throw new RuntimeException("服务间调用配置失败: " + e.getMessage(), e);
        }
        */
    }
}
