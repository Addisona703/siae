package com.hngy.siae.content.strategy.audit;

import com.hngy.siae.content.enums.TypeEnum;
import com.hngy.siae.core.asserts.AssertUtils;
import com.hngy.siae.core.result.ContentResultCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 审核处理器上下文
 * 负责管理和获取审核处理器
 * 通过 @AuditType 注解注册处理器
 * 
 * Requirements: 2.6
 * 
 * @author Kiro
 */
@Slf4j
@Component
public class AuditHandlerContext {

    private final Map<TypeEnum, AuditHandler> handlerMap = new HashMap<>();

    /**
     * 构造函数，自动注入所有 AuditHandler 实现
     * 通过 @AuditType 注解获取处理器支持的类型
     * 
     * @param handlers 所有审核处理器实现
     */
    public AuditHandlerContext(List<AuditHandler> handlers) {
        for (AuditHandler handler : handlers) {
            Class<?> targetClass = AopUtils.getTargetClass(handler);
            AuditType annotation = targetClass.getAnnotation(AuditType.class);
            if (annotation != null) {
                TypeEnum type = annotation.value();
                if (handlerMap.containsKey(type)) {
                    log.warn("审核处理器类型 [{}] 已存在，跳过重复绑定：{}", type, targetClass.getName());
                    continue;
                }
                handlerMap.put(type, handler);
            } else {
                log.warn("审核处理器 [{}] 缺少 @AuditType 注解，已跳过", targetClass.getName());
            }
        }
    }

    /**
     * 根据类型获取对应的审核处理器
     * 
     * @param type 目标类型
     * @return 对应的审核处理器
     * @throws com.hngy.siae.core.exception.BusinessException 如果未找到对应类型的处理器
     */
    public AuditHandler getHandler(TypeEnum type) {
        AuditHandler handler = handlerMap.get(type);
        AssertUtils.notNull(handler, ContentResultCodeEnum.AUDIT_HANDLER_NOT_FOUND);
        return handler;
    }

    /**
     * 检查是否存在指定类型的处理器
     * 
     * @param type 目标类型
     * @return 是否存在
     */
    public boolean hasHandler(TypeEnum type) {
        return handlerMap.containsKey(type);
    }
}
