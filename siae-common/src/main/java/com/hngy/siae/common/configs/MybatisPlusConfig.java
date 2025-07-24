package com.hngy.siae.common.configs;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;


/**
 * MyBatis Plus配置
 *
 * @author KEYKB
 * 创建时间: 2025/05/21
 */
@Configuration
public class MybatisPlusConfig {

    /***
     * 分页插件注册
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        // 如果是其他数据库换成对应类型
        return interceptor;
    }

    /**
     * 通用自动填充时间处理器
     * 支持多种字段名格式：createTime/createdAt, updateTime/updatedAt
     */
    @Component
    public static class CommonMetaObjectHandler implements MetaObjectHandler {

        @Override
        public void insertFill(MetaObject metaObject) {
            LocalDateTime now = LocalDateTime.now();

            // 支持多种创建时间字段名
            this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, now);
            this.strictInsertFill(metaObject, "createdAt", LocalDateTime.class, now);

            // 支持多种更新时间字段名
            this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, now);
            this.strictInsertFill(metaObject, "updatedAt", LocalDateTime.class, now);
        }

        @Override
        public void updateFill(MetaObject metaObject) {
            LocalDateTime now = LocalDateTime.now();

            // 支持多种更新时间字段名
            this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, now);
            this.strictUpdateFill(metaObject, "updatedAt", LocalDateTime.class, now);
        }
    }
}

