package com.hngy.siae.ai.config;

import io.micrometer.context.ContextRegistry;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import reactor.core.publisher.Hooks;

/**
 * Reactor Context Propagation 配置
 * <p>
 * 解决 Spring AI 工具函数在 Reactor 异步线程中执行时 SecurityContext 丢失的问题。
 * 通过启用 Reactor 的自动上下文传播，确保 SecurityContext 能够跨线程传递。
 *
 * @author SIAE Team
 */
@Slf4j
@Configuration
public class ReactorContextPropagationConfig {

    @PostConstruct
    public void init() {
        // 启用 Reactor 的自动上下文传播
        Hooks.enableAutomaticContextPropagation();
        
        // 注册 SecurityContext 的上下文传播器
        ContextRegistry.getInstance().registerThreadLocalAccessor(
                "SECURITY_CONTEXT",
                SecurityContextHolder::getContext,
                SecurityContextHolder::setContext,
                SecurityContextHolder::clearContext
        );
        
        log.info("Reactor context propagation enabled for SecurityContext");
    }
}
