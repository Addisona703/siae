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

## 方式一：注解自动声明（推荐）

使用 `@DeclareQueue` 注解自动创建队列、交换机和绑定，无需手动配置 YAML：

`java
import com.hngy.siae.messaging.annotation.DeclareQueue;
import com.hngy.siae.messaging.annotation.ExchangeType;

// 简单队列声明
@DeclareQueue(queue = "my.simple.queue")

// 完整声明（队列 + 交换机 + 绑定）
@DeclareQueue(
    queue = "order.queue",
    exchange = "order.exchange",
    routingKey = "order.#",
    exchangeType = ExchangeType.TOPIC
)

// 带死信队列配置
@DeclareQueue(
    queue = "task.queue",
    exchange = "task.exchange",
    routingKey = "task.*",
    deadLetterExchange = "dlx.exchange",
    deadLetterRoutingKey = "dlx.task"
)

// 多个队列声明
@DeclareQueue(queue = "queue1", exchange = "exchange1", routingKey = "key1")
@DeclareQueue(queue = "queue2", exchange = "exchange1", routingKey = "key2")
@Service
public class MyService {
    // ...
}
`

### 注解属性说明

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| queue | String | 必填 | 队列名称 |
| exchange | String | "" | 交换机名称，不填则只创建队列 |
| routingKey | String | "" | 路由键，默认使用队列名 |
| exchangeType | ExchangeType | TOPIC | 交换机类型：DIRECT/TOPIC/FANOUT/HEADERS |
| durable | boolean | true | 队列是否持久化 |
| autoDelete | boolean | false | 队列是否自动删除 |
| exclusive | boolean | false | 队列是否排他 |
| deadLetterExchange | String | "" | 死信交换机 |
| deadLetterRoutingKey | String | "" | 死信路由键 |
| messageTtl | long | 0 | 消息 TTL（毫秒） |
| maxLength | int | 0 | 队列最大长度 |

## 方式二：YAML 配置声明

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
