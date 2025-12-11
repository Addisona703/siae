package com.hngy.siae.ai.tool;

import com.hngy.siae.ai.config.AiConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 测试工具注册功能
 * 
 * @author SIAE Team
 */
@SpringBootTest(classes = {AiConfig.class, AwardQueryTool.class, MemberQueryTool.class})
@TestPropertySource(properties = {
    "siae.ai.system-prompt=You are a helpful AI assistant for SIAE.",
    "siae.ai.retry.max-attempts=3",
    "siae.ai.retry.initial-interval=1000"
})
class ToolRegistrationTest {

    @Test
    void testToolBeanCreation() {
        // 这个测试验证工具函数 bean 能够正确创建
        // 如果工具注册有问题，Spring 上下文启动会失败
        assertThat(true).isTrue(); // 如果能到这里说明 bean 创建成功
    }
}