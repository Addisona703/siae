package com.hngy.siae.auth;

import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.Arrays;
import java.util.List;

public class SpelPermissionDemo {

    public static void main(String[] args) {
        // 假设用户权限和角色
        List<String> authorities = Arrays.asList("LOG_VIEW", "USER_EDIT");
        List<String> roles = Arrays.asList("USER");

        // 构建 SpEL 上下文
        StandardEvaluationContext context = new StandardEvaluationContext();

        // 注入 hasAuthority 和 hasRole 函数
        context.setVariable("hasAuthority", (PermissionFunction) authority -> authorities.contains(authority));
        context.setVariable("hasRole", (PermissionFunction) role -> roles.contains(role));

        // 创建表达式解析器
        ExpressionParser parser = new SpelExpressionParser();

        // 表达式：必须拥有 LOG_VIEW 权限或 ADMIN 角色
        String expression = "#hasAuthority('LOG_VIEW') or #hasRole('ADMIN')";

        // 解析表达式
        Expression exp = parser.parseExpression(expression);
        Boolean result = exp.getValue(context, Boolean.class);

        System.out.println("权限判断结果: " + result);
    }

    // 函数式接口，用来模拟权限函数
    @FunctionalInterface
    interface PermissionFunction {
        boolean check(String value);
    }
}
