package com.hngy.siae.core.jackson;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.hngy.siae.core.enums.BaseEnum;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * 枚举 Jackson 模块
 * 自动扫描并注册所有 BaseEnum 实现类的序列化器和反序列化器
 * 
 * @author KEYKB
 */
@SuppressWarnings("unchecked")
public class EnumModule extends SimpleModule {
    
    private static final Logger log = LoggerFactory.getLogger(EnumModule.class);

    /**
     * 扫描指定包下的所有枚举
     * 
     * @param basePackages 要扫描的基础包路径
     */
    public EnumModule(String... basePackages) {
        super("BaseEnumModule");
        
        for (String basePackage : basePackages) {
            scanAndRegister(basePackage);
        }
    }

    private void scanAndRegister(String basePackage) {
        try {
            Reflections reflections = new Reflections(basePackage);
            Set<Class<? extends BaseEnum>> enumClasses = reflections.getSubTypesOf(BaseEnum.class);

            for (Class<? extends BaseEnum> enumClass : enumClasses) {
                if (enumClass.isEnum()) {
                    this.addDeserializer((Class) enumClass, new BaseEnumDeserializer(enumClass));
                    this.addSerializer((Class) enumClass, new BaseEnumSerializer());
                    log.debug("注册枚举序列化器: {}", enumClass.getName());
                }
            }
        } catch (Exception e) {
            log.warn("扫描枚举类失败: {}", basePackage, e);
        }
    }
}
