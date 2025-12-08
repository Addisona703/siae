package com.hngy.siae.web.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.hngy.siae.web.properties.WebProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis Plus配置
 * 支持配置化的分页、逻辑删除等功能
 * 
 * @author SIAE开发团队
 */
@Slf4j
@Configuration
@ConditionalOnClass(MybatisPlusInterceptor.class)
@ConditionalOnProperty(prefix = "siae.web.mybatis-plus", name = "enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class MybatisPlusConfig {

    private final WebProperties webProperties;

    /**
     * MyBatis Plus 拦截器配置
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        
        WebProperties.MybatisPlus config = webProperties.getMybatisPlus();
        
        // 分页插件
        if (config.isPaginationEnabled()) {
            PaginationInnerInterceptor paginationInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);
            paginationInterceptor.setMaxLimit(config.getMaxLimit());
            interceptor.addInnerInterceptor(paginationInterceptor);
            log.info("MyBatis Plus 分页插件已启用，最大限制：{}", config.getMaxLimit());
        }
        
        // 乐观锁插件（用于并发控制，需要在实体类字段上添加 @Version 注解）
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        log.info("MyBatis Plus 乐观锁插件已启用");
        
        // 防止全表更新与删除插件
        interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());
        
        return interceptor;
    }

    /**
     * 通用自动填充时间处理器
     * 支持多种字段名格式和配置化控制
     */
    @Component
    @ConditionalOnProperty(prefix = "siae.web.mybatis-plus", name = "enabled", havingValue = "true", matchIfMissing = true)
    public static class CommonMetaObjectHandler implements MetaObjectHandler {

        @Override
        public void insertFill(MetaObject metaObject) {
            LocalDateTime now = LocalDateTime.now();

            // 支持多种创建时间字段名
            this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, now);
            this.strictInsertFill(metaObject, "createdAt", LocalDateTime.class, now);
            this.strictInsertFill(metaObject, "gmtCreate", LocalDateTime.class, now);

            // 支持多种更新时间字段名
            this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, now);
            this.strictInsertFill(metaObject, "updatedAt", LocalDateTime.class, now);
            this.strictInsertFill(metaObject, "gmtModified", LocalDateTime.class, now);
            
            // 创建者字段（如果需要的话，可以从上下文获取当前用户）
            // this.strictInsertFill(metaObject, "createBy", String.class, getCurrentUserId());
        }

        @Override
        public void updateFill(MetaObject metaObject) {
            LocalDateTime now = LocalDateTime.now();

            // 支持多种更新时间字段名
            this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, now);
            this.strictUpdateFill(metaObject, "updatedAt", LocalDateTime.class, now);
            this.strictUpdateFill(metaObject, "gmtModified", LocalDateTime.class, now);
            
            // 更新者字段（如果需要的话，可以从上下文获取当前用户）
            // this.strictUpdateFill(metaObject, "updateBy", String.class, getCurrentUserId());
        }

        /**
         * 获取当前用户ID（示例方法，实际实现需要根据具体的认证方案）
         */
        private String getCurrentUserId() {
            // 这里可以从 SecurityContext、ThreadLocal 或其他方式获取当前用户ID
            // 为了避免循环依赖，这里暂时返回系统默认值
            return "system";
        }
    }
}
