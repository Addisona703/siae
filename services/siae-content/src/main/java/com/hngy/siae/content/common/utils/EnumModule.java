package com.hngy.siae.content.common.utils;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.hngy.siae.content.common.enums.BaseEnum;
import org.reflections.Reflections;

import java.util.Set;

@SuppressWarnings("unchecked")
public class EnumModule extends SimpleModule {

    public EnumModule() {
        // 扫描指定包下所有实现 BaseEnum 接口的枚举类
        Reflections reflections = new Reflections("com.hngy.siae.content.common.enums");
        Set<Class<? extends BaseEnum>> enumClasses = reflections.getSubTypesOf(BaseEnum.class);

        for (Class<? extends BaseEnum> enumClass : enumClasses) {
            if (enumClass.isEnum()) {
                this.addDeserializer((Class) enumClass, new BaseEnumDeserializer(enumClass));
                this.addSerializer((Class) enumClass, new BaseEnumSerializer(enumClass));

            }
        }
    }
}