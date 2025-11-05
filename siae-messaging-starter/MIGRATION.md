# SIAE Messaging Starter 迁移指南

## 1. 引入依赖
在业务模块 pom.xml 中加入：
`xml
<dependency>
    <groupId>com.hngy</groupId>
    <artifactId>siae-messaging-starter</artifactId>
    <version></version>
</dependency>
`

## 2. 清理旧配置
移除原先手动配置的 ConnectionFactory、RabbitTemplate、RabbitListenerContainerFactory。Starter 自动接管。

## 3. 配置 YAML
统一使用 siae.messaging.rabbit.* 前缀：
`yaml
siae:
  messaging:
    rabbit:
      connection:
        addresses: localhost:5672
        username: guest
        password: guest
      exchanges:
        foo.exchange:
          type: topic
      queues:
        foo.queue:
          durable: true
      bindings:
        foo.binding:
          exchange: foo.exchange
          destination: foo.queue
          routing-key: foo.routing
`

## 4. 修改发送入口
将原本直接调用 RabbitTemplate 的代码替换为 SiaeMessagingTemplate。

`java
@Autowired
private SiaeMessagingTemplate messagingTemplate;

messagingTemplate.send("foo.exchange", "foo.routing", payload);
`

## 5. 修改监听注解
把 @RabbitListener 改为 @SiaeRabbitListener，享受默认拦截器、错误处理、重试策略：
`java
@SiaeRabbitListener(queues = "foo.queue")
public void consume(Foo payload) { ... }
`

## 6. 动态刷新注意事项
- 仅支持连接参数（地址、凭证、VHost、SSL）热更新；队列拓扑需重启应用。
- 若使用 Spring Cloud Config，请启用 spring-cloud-context 并调用 /actuator/refresh。

## 7. 健康检查
启用 Actuator 后可通过 /actuator/health 查看 siaeRabbitHealthIndicator 状态，便于监控与报警。
