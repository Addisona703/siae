package com.hngy.siae.common.config;

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;

import static com.hngy.siae.common.config.SwaggerConstants.*;

/**
 * Swagger UI 自定义配置类
 *
 * 提供全局的API响应示例和错误码说明
 *
 * @author SIAE开发团队
 */
@Configuration
public class SwaggerUIConfig {

    /**
     * 全局OpenAPI自定义器
     */
    @Bean
    public OpenApiCustomizer globalOpenApiCustomizer() {
        return openApi -> {
            // 添加全局响应示例
            addGlobalResponses(openApi);
            // 添加全局标签排序
            if (openApi.getTags() != null) {
                openApi.getTags().sort((t1, t2) -> t1.getName().compareTo(t2.getName()));
            }
        };
    }

    /**
     * 全局操作自定义器
     */
    @Bean
    public OperationCustomizer globalOperationCustomizer() {
        return (operation, handlerMethod) -> {
            // 添加通用响应
            addCommonResponses(operation, handlerMethod);
            // 设置操作ID
            setOperationId(operation, handlerMethod);
            return operation;
        };
    }

    /**
     * 添加全局响应定义
     */
    private void addGlobalResponses(OpenAPI openApi) {
        if (openApi.getComponents() == null) {
            return;
        }

        // 添加通用响应组件
        openApi.getComponents()
                .addResponses("Success", createSuccessResponse())
                .addResponses("BadRequest", createBadRequestResponse())
                .addResponses("Unauthorized", createUnauthorizedResponse())
                .addResponses("Forbidden", createForbiddenResponse())
                .addResponses("NotFound", createNotFoundResponse())
                .addResponses("InternalServerError", createInternalServerErrorResponse());
    }

    /**
     * 为操作添加通用响应
     */
    private void addCommonResponses(Operation operation, HandlerMethod handlerMethod) {
        ApiResponses responses = operation.getResponses();
        if (responses == null) {
            responses = new ApiResponses();
            operation.setResponses(responses);
        }

        // 如果没有定义400响应，添加默认的
        if (!responses.containsKey("400")) {
            responses.addApiResponse("400", createBadRequestResponse());
        }

        // 如果没有定义500响应，添加默认的
        if (!responses.containsKey("500")) {
            responses.addApiResponse("500", createInternalServerErrorResponse());
        }

        // 对于需要认证的接口，添加401和403响应
        if (requiresAuthentication(handlerMethod)) {
            if (!responses.containsKey("401")) {
                responses.addApiResponse("401", createUnauthorizedResponse());
            }
            if (!responses.containsKey("403")) {
                responses.addApiResponse("403", createForbiddenResponse());
            }
        }
    }

    /**
     * 设置操作ID
     */
    private void setOperationId(Operation operation, HandlerMethod handlerMethod) {
        if (operation.getOperationId() == null) {
            String className = handlerMethod.getBeanType().getSimpleName();
            String methodName = handlerMethod.getMethod().getName();
            operation.setOperationId(className + "_" + methodName);
        }
    }

    /**
     * 判断是否需要认证
     */
    private boolean requiresAuthentication(HandlerMethod handlerMethod) {
        // 检查方法或类上是否有@PreAuthorize注解
        return handlerMethod.hasMethodAnnotation(org.springframework.security.access.prepost.PreAuthorize.class) ||
               handlerMethod.getBeanType().isAnnotationPresent(org.springframework.security.access.prepost.PreAuthorize.class);
    }

    /**
     * 创建成功响应
     */
    private ApiResponse createSuccessResponse() {
        return new ApiResponse()
                .description("操作成功")
                .content(new Content()
                        .addMediaType("application/json", new MediaType()
                                .addExamples("success", new Example()
                                        .summary("成功响应示例")
                                        .value(SUCCESS_EXAMPLE))));
    }

    /**
     * 创建400错误响应
     */
    private ApiResponse createBadRequestResponse() {
        return new ApiResponse()
                .description("请求参数错误")
                .content(new Content()
                        .addMediaType("application/json", new MediaType()
                                .addExamples("badRequest", new Example()
                                        .summary("参数错误示例")
                                        .value(BAD_REQUEST_EXAMPLE))));
    }

    /**
     * 创建401错误响应
     */
    private ApiResponse createUnauthorizedResponse() {
        return new ApiResponse()
                .description("未授权访问")
                .content(new Content()
                        .addMediaType("application/json", new MediaType()
                                .addExamples("unauthorized", new Example()
                                        .summary("未授权示例")
                                        .value(UNAUTHORIZED_EXAMPLE))));
    }

    /**
     * 创建403错误响应
     */
    private ApiResponse createForbiddenResponse() {
        return new ApiResponse()
                .description("权限不足")
                .content(new Content()
                        .addMediaType("application/json", new MediaType()
                                .addExamples("forbidden", new Example()
                                        .summary("权限不足示例")
                                        .value(FORBIDDEN_EXAMPLE))));
    }

    /**
     * 创建404错误响应
     */
    private ApiResponse createNotFoundResponse() {
        return new ApiResponse()
                .description("资源不存在")
                .content(new Content()
                        .addMediaType("application/json", new MediaType()
                                .addExamples("notFound", new Example()
                                        .summary("资源不存在示例")
                                        .value(NOT_FOUND_EXAMPLE))));
    }

    /**
     * 创建500错误响应
     */
    private ApiResponse createInternalServerErrorResponse() {
        return new ApiResponse()
                .description("服务器内部错误")
                .content(new Content()
                        .addMediaType("application/json", new MediaType()
                                .addExamples("internalServerError", new Example()
                                        .summary("服务器错误示例")
                                        .value(INTERNAL_ERROR_EXAMPLE))));
    }
}
