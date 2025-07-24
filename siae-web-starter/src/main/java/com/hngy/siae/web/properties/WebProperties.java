package com.hngy.siae.web.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Web相关配置属性
 * 
 * @author SIAE开发团队
 */
@Data
@ConfigurationProperties(prefix = "siae.web")
public class WebProperties {

    /**
     * 统一响应处理配置
     */
    private Response response = new Response();

    /**
     * Jackson序列化配置
     */
    private Jackson jackson = new Jackson();

    /**
     * 全局异常处理配置
     */
    private Exception exception = new Exception();

    /**
     * MyBatis Plus配置
     */
    private MybatisPlus mybatisPlus = new MybatisPlus();

    /**
     * 统一响应处理配置
     */
    @Data
    public static class Response {
        /**
         * 是否启用统一响应处理
         */
        private boolean enabled = true;

        /**
         * 需要包装响应的包路径
         */
        private String[] basePackages = {"com.hngy.siae"};

        /**
         * 排除的路径模式
         */
        private String[] excludePatterns = {"/actuator/**", "/swagger-ui/**", "/v3/api-docs/**"};
    }

    /**
     * Jackson序列化配置
     */
    @Data
    public static class Jackson {
        /**
         * 是否启用Jackson配置
         */
        private boolean enabled = true;

        /**
         * 日期格式
         */
        private String dateFormat = "yyyy-MM-dd HH:mm:ss";

        /**
         * 时区
         */
        private String timeZone = "GMT+8";

        /**
         * 是否序列化null值
         */
        private boolean serializeNulls = false;
    }

    /**
     * 全局异常处理配置
     */
    @Data
    public static class Exception {
        /**
         * 是否启用全局异常处理
         */
        private boolean enabled = true;

        /**
         * 是否打印异常堆栈
         */
        private boolean printStackTrace = true;

        /**
         * 是否返回详细错误信息（生产环境建议关闭）
         */
        private boolean includeStackTrace = false;
    }

    /**
     * MyBatis Plus配置
     */
    @Data
    public static class MybatisPlus {
        /**
         * 是否启用MyBatis Plus配置
         */
        private boolean enabled = true;

        /**
         * 是否启用分页插件
         */
        private boolean paginationEnabled = true;

        /**
         * 分页插件最大限制数
         */
        private long maxLimit = 1000L;

        /**
         * 是否启用逻辑删除
         */
        private boolean logicDeleteEnabled = true;

        /**
         * 逻辑删除字段名
         */
        private String logicDeleteField = "deleted";

        /**
         * 逻辑删除值（已删除）
         */
        private String logicDeleteValue = "1";

        /**
         * 逻辑未删除值（未删除）
         */
        private String logicNotDeleteValue = "0";
    }
}
