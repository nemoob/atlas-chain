# Atlas-Chain 开发者使用指南

## 概述

Atlas-Chain 是一个基于 Java 的责任链框架，提供了灵活、易用的责任链模式实现。框架支持核心模块独立使用和 Spring Boot 无缝集成，适用于各种业务场景下的流程处理需求。

## 核心特性

- **模块化设计**：核心模块可独立使用，Spring Boot Starter 提供自动配置
- **注解驱动**：基于 `@ChainHandler` 注解的自动注册机制
- **类型安全**：泛型约束确保请求参数和响应结果的类型安全
- **灵活控制**：支持处理者跳过机制和链路中断控制
- **异步支持**：支持同步/异步执行模式，可自定义线程池
- **易于扩展**：简洁的 API 设计，便于添加新的处理逻辑

## 快速开始

### 1. 添加依赖

#### 纯 Java 项目（使用核心模块）

```xml
<dependency>
    <groupId>io.github.nemoob</groupId>
    <artifactId>atlas-chain-core</artifactId>
    <version>0.0.1</version>
</dependency>
```

#### Spring Boot 项目（使用 Starter）

```xml
<dependency>
    <groupId>io.github.nemoob</groupId>
    <artifactId>atlas-chain-spring-boot-starter</artifactId>
    <version>0.0.1</version>
</dependency>
```

### 2. 定义请求和响应类

```java
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRequest {
    private String username;
    private String password;
    private Long userId;
}

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
    private String message;
    private boolean success = true;
}
```

## 核心组件详解

### 1. HandlerContext（处理上下文）

`HandlerContext<P, R>` 是责任链中数据传递的载体，包含：

- **request**：请求参数（泛型 P）
- **response**：响应结果（泛型 R）
- **attributes**：处理者间共享数据的键值对集合

```java
// 创建上下文
HandlerContext<UserRequest, UserResponse> context = 
    new HandlerContext<>(request, response);

// 设置共享属性
context.setAttribute("userId", "12345");
context.setAttribute("authResult", true);

// 类型安全的属性获取
String userId = context.getAttribute("userId", String.class);
Boolean authResult = context.getAttribute("authResult", Boolean.class);
```

**最佳实践：使用常量类约束属性键名**

```java
public class AuthContextKeys {
    public static final String USER_ID = "userId";
    public static final String USER_ROLE = "userRole";
    public static final String AUTH_RESULT = "authResult";
}

// 使用时
context.setAttribute(AuthContextKeys.USER_ID, "12345");
```

### 2. BaseHandler（处理者基类）

所有处理者都需要继承 `BaseHandler<P, R>` 抽象类：

```java
public class AuthenticationHandler extends BaseHandler<UserRequest, UserResponse> {
    
    @Override
    protected boolean doHandle(HandlerContext<UserRequest, UserResponse> context) {
        UserRequest request = context.getRequest();
        
        // 执行认证逻辑
        if (isValidUser(request.getUsername(), request.getPassword())) {
            context.setAttribute(AuthContextKeys.USER_ID, "12345");
            context.setAttribute(AuthContextKeys.AUTH_RESULT, true);
            return true; // 继续执行下一个处理者
        } else {
            context.setResponse(new UserResponse("Authentication failed", false));
            return false; // 中断责任链
        }
    }
    
    @Override
    public boolean shouldSkip(HandlerContext<UserRequest, UserResponse> context) {
        // 可选：判断是否跳过当前处理者
        return false;
    }
    
    @Override
    public void onCompleted(HandlerContext<UserRequest, UserResponse> context) {
        // 可选：处理完成后的回调
        log.info("Authentication completed for user: {}", 
                context.getRequest().getUsername());
    }
    
    @Override
    public void onError(HandlerContext<UserRequest, UserResponse> context, Exception e) {
        // 可选：处理异常的回调
        log.error("Authentication error: {}", e.getMessage(), e);
    }
    
    private boolean isValidUser(String username, String password) {
        // 实际的认证逻辑
        return "testUser".equals(username) && "testPassword".equals(password);
    }
}
```

**关键方法说明：**

- `doHandle()`：**必须实现**，核心业务逻辑，返回 `true` 继续执行，`false` 中断链路
- `shouldSkip()`：可选实现，判断是否跳过当前处理者
- `onCompleted()`：可选实现，处理完成后的回调
- `onError()`：可选实现，处理异常时的回调

### 3. ChainRegistry（链注册器）

负责处理者的注册和管理：

```java
// 创建注册器
ChainRegistry<UserRequest, UserResponse> registry = new ChainRegistry<>();

// 注册处理者到指定链路
String chainId = "user-process-chain";
registry.registerHandler(chainId, new AuthenticationHandler());
registry.registerHandler(chainId, new AuthorizationHandler());
registry.registerHandler(chainId, new BusinessLogicHandler());

// 获取所有注册的链ID
Set<String> chainIds = registry.getAllChainIds();
```

### 4. ChainExecutor（链执行器）

负责责任链的执行：

```java
// 创建执行器
ChainExecutor<UserRequest, UserResponse> executor = new ChainExecutor<>(registry);

// 同步执行
try {
    UserResponse response = executor.execute(chainId, context);
    System.out.println("Result: " + response.getMessage());
} catch (Exception e) {
    System.err.println("Execution failed: " + e.getMessage());
}

// 异步执行
CompletableFuture<UserResponse> future = executor.executeAsync(chainId, context);
future.thenAccept(response -> {
    System.out.println("Async result: " + response.getMessage());
}).exceptionally(throwable -> {
    System.err.println("Async execution failed: " + throwable.getMessage());
    return null;
});

// 关闭执行器
executor.shutdown();
```

## 使用方式

### 方式一：核心模块独立使用

适用于纯 Java 项目或不使用 Spring 的场景：

```java
public class CoreExampleApplication {
    public static void main(String[] args) throws Exception {
        // 1. 创建注册器
        ChainRegistry<UserRequest, UserResponse> registry = new ChainRegistry<>();
        
        // 2. 创建处理者
        AuthenticationHandler authHandler = new AuthenticationHandler();
        AuthorizationHandler authzHandler = new AuthorizationHandler();
        BusinessLogicHandler businessHandler = new BusinessLogicHandler();
        
        // 3. 注册处理者到指定链路
        String chainId = "user-process-chain";
        registry.registerHandler(chainId, authHandler);
        registry.registerHandler(chainId, authzHandler);
        registry.registerHandler(chainId, businessHandler);
        
        // 4. 创建执行器
        ChainExecutor<UserRequest, UserResponse> executor = new ChainExecutor<>(registry);
        
        // 5. 创建上下文并执行
        UserRequest request = new UserRequest("testUser", "testPassword", null);
        HandlerContext<UserRequest, UserResponse> context = 
            new HandlerContext<>(request, new UserResponse());
        
        UserResponse response = executor.execute(chainId, context);
        System.out.println("Response: " + response.getMessage());
        
        // 6. 关闭执行器
        executor.shutdown();
    }
}
```

### 方式二：Spring Boot 集成使用

适用于 Spring Boot 项目，提供自动配置和注解驱动：

#### 1. 配置文件

```yaml
# application.yml
chain:
  handler:
    core-pool-size: 5      # 核心线程数
    max-pool-size: 10      # 最大线程数
    keep-alive-time: 60    # 线程空闲时间（秒）
    queue-capacity: 100    # 队列容量

logging:
  level:
    io.github.nemoob.atlas.chain: DEBUG
```

#### 2. 实现处理者（使用注解）

```java
@ChainHandler(value = "user-process", order = 1)
@Component
@Slf4j
public class UserValidateHandler extends BaseHandler<UserRequest, UserResponse> {
    
    @Override
    protected boolean doHandle(HandlerContext<UserRequest, UserResponse> context) {
        UserRequest request = context.getRequest();
        
        // 参数验证
        if (request.getUserId() == null || request.getUsername() == null) {
            context.setResponse(new UserResponse("Validation failed: missing required fields", false));
            return false; // 验证失败，终止链路
        }
        
        context.setAttribute("validated", true);
        log.info("User validation passed for: {}", request.getUsername());
        return true;
    }
}

@ChainHandler(value = "user-process", order = 2)
@Component
@Slf4j
public class UserProcessHandler extends BaseHandler<UserRequest, UserResponse> {
    
    @Autowired
    private UserService userService;
    
    @Override
    protected boolean doHandle(HandlerContext<UserRequest, UserResponse> context) {
        UserRequest request = context.getRequest();
        
        // 业务处理
        try {
            String result = userService.processUser(request);
            context.setResponse(new UserResponse(result, true));
            log.info("User processing completed: {}", result);
            return true;
        } catch (Exception e) {
            context.setResponse(new UserResponse("Processing failed: " + e.getMessage(), false));
            return false;
        }
    }
}
```

#### 3. 在服务中使用

```java
@Service
@Slf4j
public class UserService {
    
    @Autowired
    private ChainExecutor<UserRequest, UserResponse> chainExecutor;
    
    public UserResponse processUser(UserRequest request) {
        HandlerContext<UserRequest, UserResponse> context = 
            new HandlerContext<>(request, new UserResponse());
        
        try {
            return chainExecutor.execute("user-process", context);
        } catch (Exception e) {
            log.error("Chain execution failed", e);
            return new UserResponse("System error: " + e.getMessage(), false);
        }
    }
    
    public CompletableFuture<UserResponse> processUserAsync(UserRequest request) {
        HandlerContext<UserRequest, UserResponse> context = 
            new HandlerContext<>(request, new UserResponse());
        
        return chainExecutor.executeAsync("user-process", context)
            .exceptionally(throwable -> {
                log.error("Async chain execution failed", throwable);
                return new UserResponse("System error: " + throwable.getMessage(), false);
            });
    }
}
```

#### 4. 控制器使用

```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @PostMapping("/process")
    public ResponseEntity<UserResponse> processUser(@RequestBody UserRequest request) {
        UserResponse response = userService.processUser(request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/process-async")
    public CompletableFuture<ResponseEntity<UserResponse>> processUserAsync(
            @RequestBody UserRequest request) {
        return userService.processUserAsync(request)
            .thenApply(ResponseEntity::ok);
    }
}
```

## 高级特性

### 1. 自定义线程池

```java
// 核心模块自定义线程池
ExecutorService customExecutor = Executors.newFixedThreadPool(20);
ChainExecutor<UserRequest, UserResponse> executor = 
    new ChainExecutor<>(registry, customExecutor);

// Spring Boot 中通过配置自定义
@Configuration
public class ChainConfig {
    
    @Bean
    @Primary
    public ExecutorService chainExecutorService() {
        return new ThreadPoolExecutor(
            10, // 核心线程数
            50, // 最大线程数
            60L, TimeUnit.SECONDS, // 空闲时间
            new LinkedBlockingQueue<>(200), // 队列
            new ThreadFactoryBuilder().setNameFormat("chain-executor-%d").build()
        );
    }
}
```

### 2. 条件跳过处理者

```java
@ChainHandler(value = "user-process", order = 3)
@Component
public class OptionalHandler extends BaseHandler<UserRequest, UserResponse> {
    
    @Override
    public boolean shouldSkip(HandlerContext<UserRequest, UserResponse> context) {
        // 根据上下文条件决定是否跳过
        Boolean skipOptional = context.getAttribute("skipOptional", Boolean.class);
        return Boolean.TRUE.equals(skipOptional);
    }
    
    @Override
    protected boolean doHandle(HandlerContext<UserRequest, UserResponse> context) {
        // 可选的处理逻辑
        log.info("Executing optional handler");
        return true;
    }
}
```

### 3. 异常处理策略

```java
@ChainHandler(value = "user-process", order = 4)
@Component
public class RobustHandler extends BaseHandler<UserRequest, UserResponse> {
    
    @Override
    protected boolean doHandle(HandlerContext<UserRequest, UserResponse> context) {
        try {
            // 可能抛出异常的业务逻辑
            riskyOperation(context);
            return true;
        } catch (BusinessException e) {
            // 业务异常，设置错误响应但继续执行
            log.warn("Business exception in handler: {}", e.getMessage());
            context.setAttribute("hasWarning", true);
            return true;
        }
        // 其他异常会被框架捕获并调用 onError
    }
    
    @Override
    public void onError(HandlerContext<UserRequest, UserResponse> context, Exception e) {
        log.error("Critical error in robust handler", e);
        // 可以设置默认响应或进行补偿操作
        context.setResponse(new UserResponse("Service temporarily unavailable", false));
    }
    
    private void riskyOperation(HandlerContext<UserRequest, UserResponse> context) 
            throws BusinessException {
        // 实际的业务逻辑
    }
}
```

## 最佳实践

### 1. 处理者设计原则

- **单一职责**：每个处理者只负责一个特定的业务逻辑
- **明确返回值**：`doHandle` 方法应明确返回 `true`/`false`，避免歧义
- **合理顺序**：通过 `order` 属性合理安排处理者执行顺序
- **异常处理**：合理使用 `onError` 回调处理异常情况

### 2. 上下文使用建议

- **类型安全**：使用常量类约束 `attributes` 的键名
- **数据传递**：合理利用上下文在处理者间传递数据
- **避免过大对象**：上下文中避免存储过大的对象，影响性能
- **清理资源**：在适当的时候清理不再需要的属性

### 3. 性能优化

- **线程池调优**：根据业务特点调整线程池参数
- **链路设计**：合理设计处理者顺序，避免不必要的处理
- **异步执行**：对于耗时操作，优先考虑异步执行
- **监控日志**：开启适当的日志记录，便于问题排查

### 4. 错误处理策略

```java
// 全局异常处理
@ControllerAdvice
public class ChainExceptionHandler {
    
    @ExceptionHandler(ChainExecutionException.class)
    public ResponseEntity<ErrorResponse> handleChainException(ChainExecutionException e) {
        log.error("Chain execution failed", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ErrorResponse("CHAIN_ERROR", e.getMessage()));
    }
}
```

## 常见问题

### Q1: 如何调试责任链执行过程？

**A**: 开启 DEBUG 日志级别，框架会输出详细的执行信息：

```yaml
logging:
  level:
    io.github.nemoob.atlas.chain: DEBUG
```

### Q2: 处理者执行顺序如何确定？

**A**: 在 Spring Boot 中通过 `@ChainHandler` 的 `order` 属性控制，数值越小优先级越高。核心模块中按注册顺序执行。

### Q3: 如何实现条件分支？

**A**: 通过 `shouldSkip()` 方法或在 `doHandle()` 中根据条件返回不同值：

```java
@Override
protected boolean doHandle(HandlerContext<UserRequest, UserResponse> context) {
    if (someCondition) {
        // 执行分支A逻辑
        return true;
    } else {
        // 跳过后续处理或执行分支B逻辑
        return false;
    }
}
```

### Q4: 如何处理循环依赖？

**A**: 避免在处理者中直接注入其他处理者，通过上下文传递数据或使用事件机制。

### Q5: 性能如何？

**A**: 框架本身开销很小，主要性能取决于具体的业务逻辑。建议：
- 使用异步执行处理耗时操作
- 合理设计处理者顺序
- 避免在上下文中存储大对象

## 总结

Atlas-Chain 提供了一套完整、灵活的责任链解决方案。通过合理的设计和配置，可以大大简化复杂业务流程的实现，提高代码的可维护性和可扩展性。

无论是独立使用还是与 Spring Boot 集成，Atlas-Chain 都能满足不同场景下的业务需求。框架的模块化设计使其能够适应各种复杂的业务场景，是 Java 开发者处理责任链模式的理想选择。