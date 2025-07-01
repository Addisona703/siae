package com.hngy.siae.common.configs;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


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
}

