### 显示结构树 : `tree /f /a`
### 启动配置中心: `startup.cmd -m standalone`
### 常见的注释标签：
```md
// TODO: 描述
标记“待办事项”，提醒后续要做的事情。

// FIXME: 描述
标记“需要修复的问题”。

// NOTE: 描述
备注说明。

// BUG: 描述
标记有缺陷的代码。
```
---
### Jackson配置
简单的基础规则配置再yaml中（再这里可以统一配置到 Nacos 中），复杂的配置到代码中
1. 代码配置

| 配置项                      | 说明                                              | 代码示例或配置示例                                           |
| --------------------------- | ------------------------------------------------- | ------------------------------------------------------------ |
| 时间格   式                 | 统一日期时间的序列化格式                          | `builder.simpleDateFormat("yyyy-MM-dd HH:mm:ss")`            |
| 时区                        | 设置默认时区，避免时区偏差                        | `builder.timeZone(TimeZone.getTimeZone("Asia/Shanghai"))`    |
| 序列化时忽略空字段          | 不序列化值为 `null` 的字段                        | `com.hngy.siae.content.mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)` 或 `spring.jackson.default-property-inclusion: NON_NULL` |
| 序列化时忽略默认值字段      | 忽略字段为默认值时的序列化                        | `JsonInclude.Include.NON_DEFAULT`                            |
| 反序列化时忽略未知字段      | JSON 中有，但对象没有的字段，忽略，避免异常       | `com.hngy.siae.content.mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)` 或 `spring.jackson.deserialization.fail-on-unknown-properties: false` |
| 允许单引号                  | JSON 字符串支持单引号                             | `com.hngy.siae.content.mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true)` |
| 允许不带引号字段名          | JSON 支持字段名不加引号                           | `com.hngy.siae.content.mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)` |
| 允许反斜杠转义字符          | 允许字符串中使用反斜杠转义                        | `com.hngy.siae.content.mapper.configure(JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER, true)` |
| 序列化枚举为字符串          | 枚举默认序列化为字符串名称                        | `com.hngy.siae.content.mapper.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING)` |
| 反序列化枚举用字符串        | 枚举反序列化时用字符串匹配                        | `com.hngy.siae.content.mapper.enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING)` |
| 关闭时间戳序列化            | 不用时间戳序列化日期，改用可读字符串              | `com.hngy.siae.content.mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)` 或 `spring.jackson.serialization.write-dates-as-timestamps: false` |
| 支持 Java 8 日期类型        | 支持 `java.time` 包下的日期类型序列化             | 引入 `jackson-datatype-jsr310` 模块，并注册模块              |
| JSON 美化输出               | 输出格式化（换行缩进）                            | `com.hngy.siae.content.mapper.enable(SerializationFeature.INDENT_OUTPUT)` 或 `spring.jackson.serialization.indent-output: true` |
| 属性命名策略                | JSON 字段命名风格转换（如驼峰转下划线）           | `com.hngy.siae.content.mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)` |
| 允许空字符串反序列化为 null | 空字符串当作 null 处理                            | 自定义反序列化器，Jackson 默认不支持                         |
| 支持多态类型序列化          | 通过 `@JsonTypeInfo` 支持接口或抽象类的多态序列化 | 需加注解或自定义模块                                         |

1. yaml配置

```yaml
spring:
  jackson:
    serialization:
      indent-output: true               # 美化输出
      write-dates-as-timestamps: false # 不使用时间戳序列化日期
      fail-on-empty-beans: false        # 避免序列化空Bean报错
    deserialization:
      fail-on-unknown-properties: false # 忽略未知字段
    default-property-inclusion: non_null  # 忽略 null 字段
    time-zone: Asia/Shanghai               # 默认时区
    date-format: yyyy-MM-dd HH:mm:ss      # 默认日期格式
    property-naming-com.hngy.siae.content.strategy: SNAKE_CASE  # 驼峰转下划线
```

---
### Mybatis-plus
1.参数回填、使得不只能回填id