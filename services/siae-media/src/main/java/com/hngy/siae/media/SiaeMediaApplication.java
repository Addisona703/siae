package com.hngy.siae.media;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * SIAE Media Service 启动类
 * 提供文件上传、存储、处理和分发的核心服务
 * <p>
 * 功能模块：
 * - 文件上传（单文件、分片上传）
 * - 文件存储（MinIO对象存储）
 * - 媒体处理（转码、缩略图、水印）
 * - 文件扫描（病毒扫描、内容审核）
 * - 配额管理（存储配额、流量配额）
 * - 生命周期管理（自动归档、清理）
 *
 * @author SIAE Team
 */
@SpringBootApplication(scanBasePackages = "com.hngy.siae")
@EnableDiscoveryClient
@EnableFeignClients
@EnableScheduling
@MapperScan("com.hngy.siae.media.repository")
public class SiaeMediaApplication {

    public static void main(String[] args) {
        SpringApplication.run(SiaeMediaApplication.class, args);
    }

}
