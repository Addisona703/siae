package com.hngy.siae.auth.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码编码器工具类
 * 用于生成和验证BCrypt密码哈希
 */
public class PasswordEncoderTest {

    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "123456";

        // 生成BCrypt哈希
        String encoded = encoder.encode(password);

        System.out.println("=== 密码编码测试 ===");
        System.out.println("原始密码: " + password);
        System.out.println("BCrypt哈希: " + encoded);
        System.out.println("验证结果: " + encoder.matches(password, encoded));
        System.out.println("==================");

        // 验证现有SQL中的哈希值
        String sqlHash = "$2a$10$fW.x.d.v.bIu2iZ8t.G2d.6tX3U.i7G8l.D05D.B.w.bC8v7fI3y2";
        System.out.println("SQL中的哈希验证: " + encoder.matches(password, sqlHash));

        // 生成多个哈希值供选择
        System.out.println("\n=== 生成新的哈希值 ===");
        for (int i = 0; i < 3; i++) {
            String newHash = encoder.encode(password);
            System.out.println("哈希值 " + (i + 1) + ": " + newHash);
            System.out.println("验证: " + encoder.matches(password, newHash));
        }
    }
}
