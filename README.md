# Atlas-Chain 责任链框架

一个基于Java的责任链框架，支持核心模块独立使用和Spring Boot集成。

## 项目结构

```
atlas-chain/
├── atlas-chain-core/              # 核心模块，无Spring依赖，可独立使用
├── atlas-chain-spring-boot-starter/ # Spring Boot集成模块
├── examples/                      # 使用示例
│   ├── core-example/              # core模块使用示例
│   └── spring-example/            # spring模块使用示例
└── pom.xml                        # 父项目POM
```

## 模块说明

### atlas-chain-core（核心模块）

核心模块不依赖Spring框架，可以在任何Java 8+环境中独立使用。

#### 主要组件

1. **HandlerContext<P, R>** - 责任链上下文，用于在处理者之间传递数据
2. **BaseHandler<P, R>** - 抽象处理者类，所有具体处理者需要继承此类
3. **ChainRegistry<P, R>** - 链注册器，负责注册和管理处理者
4. **ChainExecutor<P, R>** - 链执行器，负责执行责任链

### atlas-chain-spring-boot-starter（Spring Boot集成模块）

Spring Boot集成模块提供了自动配置和注解驱动功能。

#### 主要组件

1. **@ChainHandler** - 注解，用于标记处理者类
2. **ChainHandlerAutoConfiguration** - 自动配置类
3. **ChainHandlerProperties** - 配置属性类
4. **ChainHandlerRegistrar** - 处理者注册器

## 使用方法

### Maven依赖

对于纯Java项目使用core模块：

```xml
<dependency>
    <groupId>io.github.nemoob</groupId>
    <artifactId>atlas-chain-core</artifactId>
    <version>0.0.1</version>
</dependency>
```

对于Spring Boot项目使用starter模块：

```xml
<dependency>
    <groupId>io.github.nemoob</groupId>
    <artifactId>atlas-chain-spring-boot-starter</artifactId>
    <version>0.0.1</version>
</dependency>
```

### 核心模块使用示例

```java
// 创建注册器
ChainRegistry<UserRequest, UserResponse> registry = new ChainRegistry<>();

// 创建处理者
AuthenticationHandler authHandler = new AuthenticationHandler();
AuthorizationHandler authzHandler = new AuthorizationHandler();
BusinessLogicHandler businessHandler = new BusinessLogicHandler();

// 注册处理者到指定链路
String chainId = "user-process-chain";
registry.registerHandler(chainId, authHandler);
registry.registerHandler(chainId, authzHandler);
registry.registerHandler(chainId, businessHandler);

// 创建执行器
ChainExecutor<UserRequest, UserResponse> executor = new ChainExecutor<>(registry);

// 创建上下文
UserRequest request = new UserRequest("testUser", "testPassword");
HandlerContext<UserRequest, UserResponse> context = new HandlerContext<>(request, null);

// 执行责任链
UserResponse response = executor.execute(chainId, context);

// 关闭执行器
executor.shutdown();
```

### Spring Boot集成使用示例

1. 在处理者类上添加@ChainHandler注解：

```java
@ChainHandler(value = "user-process", order = 1)
@Component
public class UserValidateHandler extends BaseHandler<UserRequest, UserResponse> {
    @Override
    protected boolean doHandle(HandlerContext<UserRequest, UserResponse> context) {
        // 处理逻辑
        return true;
    }
}
```

2. 在服务类中注入ChainExecutor并使用：

```java
@Service
public class UserService {
    
    @Autowired
    private ChainExecutor<UserRequest, UserResponse> chainExecutor;
    
    public UserResponse processUser(Long userId) {
        UserRequest request = new UserRequest(userId, "testUser");
        HandlerContext<UserRequest, UserResponse> context = new HandlerContext<>(request, null);
        return chainExecutor.execute("user-process", context);
    }
}
```

## 配置属性

在Spring Boot项目中，可以通过以下属性配置线程池：

```yaml
chain:
  handler:
    core-pool-size: 5
    max-pool-size: 10
    keep-alive-time: 60
    queue-capacity: 100
```

## 特性

1. **基于注解的链路注册机制** - 通过@ChainHandler注解和ID执行责任链
2. **节点跳过机制** - boolean doHandle返回false时终止后续节点执行
3. **异步执行机制** - 支持异步执行，可配置自定义线程池
4. **泛型约束** - HandlerContext使用泛型约束，支持参数和响应类型分离
5. **类型安全属性** - 节点间通信属性支持类型约束
6. **模块化设计** - core包(独立使用) + spring-boot-starter包
7. **兼容性** - JDK 1.8 + Spring Boot 2.2 + Spring 5.2
8. **代码简化** - 全包使用Lombok，添加完整注释

## 构建和测试

```bash
# 构建整个项目
mvn clean install

# 运行核心模块示例
mvn exec:java -pl examples/core-example

# 运行Spring Boot示例
mvn spring-boot:run -pl examples/spring-example

# 运行测试
mvn test
```