# 公共包

### 1.configs
> 存放通用复杂配置

##### 1.1 MybatisPlus配置 - 分页插件

##### 1.2 Jackson配置（JacksonConfig）
```markdown
简单配置：配置到Nacos统一管理
注册自定义枚举模块（定制枚举序列化的规则和反序列化规则）
1. 编写规则类，继承SimpleModule
2. 编写配置类注册这个规则
```

### 2.advice
> 一般用于存放：
> + `统一响应体处理类`：实现 ResponseBodyAdvice 用来统一包装返回结果
> + `切面类`：用于 AOP 拦截逻辑，如日志记录、权限校验、性能监控等

##### 2.1 UnifiedResponseAdvice 统一响应体处理类
让你的所有接口返回值自动变成统一格式 Result<T>，不用每次都手写封装代码，还能防止忘记写、写错格式。


### 3.annotation
> 存放自定义注解

##### 3.1 UnifiedResponse 开启统一响应包装器的开关
只有加了该注解的类才会自动处理响应体


### 4.asserts
> 存放断言

##### 4.1 AsserUtils 断言抛异常
使得抛异常非常方便，代码更优雅，里面有自定义错误码枚举静态方法，属于是既有了4.2自定义错误码，也保持简洁，在这我们使用的是当前4.1的增强断言。

##### 4.2 ExceptionAssert 接口
这是一个更高级的断言机制，利用 Java 接口的默认方法，把每个错误码都变成一个可抛异常的断言对象。
你可以给 ResultCodeEnum 加上这个接口，就能写出：
```java
ResultCodeEnum.USER_NOT_FOUND.assertThrow(true);
```

##### 4.3 AssertExceptionFactory
这个是为了将来国际化、多语言扩展准备的。如果你未来要支持多语言错误提示，或者错误码映射到前端国际化 key，可以把抛异常的逻辑集中在这个工厂中。\
这个对目前项目不是必须，可以先不实现，等你国际化或提示多样化时再考虑。


### 5.dto
> 通用数据传输对象


### 6.exception
> 存放全局异常处理器，和自定义业务异常


### 7.feign
> 存放feign的一些配置和工具

##### 7.1 DefaultFeignConfig
Feign 客户端的全局默认配置 \
需要注意一下：\
@EnableFeignClients(defaultConfiguration = DefaultFeignConfig.class) 放到启动类头上，指定默认配置
> 这个类不应该被 @SpringBootApplication 扫描到，通常通过 @EnableFeignClients 的 defaultConfiguration 属性指定

##### 7.2 FeignResultDecoder
自定义 Feign 响应解码器, 可以自动拆包 Result<T> 并处理业务异常


### 8.result
> 存放统一响应类和错误码


### 9.utils
> 存放通用工具类

##### 9.1 PageConvertUtil 
分页工具类，方便将 MyBatis-Plus 的 IPage 转换成通用的 PageResult \
新增了将 MyBatis-Plus 的 IPage<E> 按映射器转换为 PageVO<V>方法 \
例子：
```java
// 假设service层调用MP分页查询
IPage<User> userPage = userMapper.selectPage(new Page<>(pageNum, pageSize), queryWrapper);

// 转换成统一响应结果
PageResult<UserVO> pageResult = PageConvertUtil.convert(userPage);

// 你也可以做DTO转换，这里假设User和UserVO结构相同或你有映射逻辑
return Result.success(pageResult);
```



### 10.validation
> mybatis-plus的分组校验接口


