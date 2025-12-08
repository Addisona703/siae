package com.hngy.siae.content.strategy.content;

import com.hngy.siae.content.enums.ContentTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 内容策略上下文
 * 负责管理和获取内容策略
 * 通过 @StrategyType 注解注册策略
 *
 * @author KEYKB
 * &#064;date: 2025/05/19
 */
@Slf4j
@Component
public class ContentStrategyContext {

    private final Map<ContentTypeEnum, ContentStrategy> strategyMap = new HashMap<>();

    @Autowired
    public ContentStrategyContext(List<ContentStrategy> strategyList) {
        for (ContentStrategy strategy : strategyList) {
            Class<?> targetClass = AopUtils.getTargetClass(strategy);
            ContentType annotation = targetClass.getAnnotation(ContentType.class);
            if (annotation != null) {
                ContentTypeEnum type = annotation.value();
                if (strategyMap.containsKey(type)) {
                    log.warn("策略类型 [{}] 已存在，跳过重复绑定：{}", type, targetClass.getName());
                    continue;
                }
                strategyMap.put(type, strategy);
            } else {
                log.warn("策略类 [{}] 缺少 @StrategyType 注解，已跳过", targetClass.getName());
            }
        }
    }

    /**
     * 获取策略
     *
     * @param type 内容类型
     * @return 对应的策略实现
     * @throws IllegalArgumentException 如果找不到对应策略
     */
    public ContentStrategy getStrategy(ContentTypeEnum type) {
        ContentStrategy strategy = strategyMap.get(type);
        if (strategy == null) {
            throw new IllegalArgumentException("不支持的内容类型：" + type);
        }
        return strategy;
    }

    /**
     * 尝试获取策略
     *
     * @param type 内容类型
     * @return 对应的策略实现（可能为空）
     */
    public Optional<ContentStrategy> tryGetStrategy(ContentTypeEnum type) {
        return Optional.ofNullable(strategyMap.get(type));
    }
}
