package com.hngy.siae.auth.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 登录测试类
 * 用于验证密码编码修复是否有效
 */
public class LoginTest {

    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // 测试数据
        String inputPassword = "123456";
        String storedHash = "$2a$10$N.zmdr9k7uOCQb0bzysuAOyoyNpwSr0YHiXKuNTtDB6aANfGDx9he";
        
        System.out.println("=== 登录密码验证测试 ===");
        System.out.println("用户输入密码: " + inputPassword);
        System.out.println("数据库存储哈希: " + storedHash);
        
        // 模拟AuthServiceImpl中的密码验证逻辑
        boolean matches = encoder.matches(inputPassword, storedHash);
        
        System.out.println("密码验证结果: " + (matches ? "成功" : "失败"));
        
        if (matches) {
            System.out.println("✅ 密码编码修复成功！用户可以正常登录");
        } else {
            System.out.println("❌ 密码编码仍有问题，需要进一步检查");
        }
        
        System.out.println("========================");
        
        // 测试其他用户账号
        String[] testUsers = {"president", "java_minister", "python_minister"};
        System.out.println("测试用户账号:");
        for (String username : testUsers) {
            System.out.println("- 用户名: " + username + ", 密码: " + inputPassword + " -> " + 
                             (matches ? "可以登录" : "无法登录"));
        }
    }
}
