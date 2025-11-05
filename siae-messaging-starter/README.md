# SIAE Messaging Starter

统一 RabbitMQ 接入组件，提供自动配置、消息发送/消费封装、动态刷新与健康检查能力。

## 快速开始
`xml
<dependency>
    <groupId>com.hngy</groupId>
    <artifactId>siae-messaging-starter</artifactId>
    <version></version>
</dependency>
`

示例配置：
`yaml
siae:
  messaging:
    rabbit:
      connection:
        addresses: localhost:5672
        username: guest
        password: guest
      exchanges:
        demo.exchange:
          type: topic
      queues:
        demo.queue:
          durable: true
      bindings:
        demo.binding:
          exchange: demo.exchange
          destination: demo.queue
          routing-key: demo.routing
`

发送示例：
`java
@Autowired
private SiaeMessagingTemplate messagingTemplate;

public void publish(String payload) {
    messagingTemplate.send("demo.exchange", "demo.routing", payload);
}
`

消费示例：
`java
@SiaeRabbitListener(queues = "demo.queue")
public void handle(String payload) {
    log.info("received: {}", payload);
}
`

更多示例参见 examples/demo-application。
