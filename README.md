# Atlas-Chain 责任链框架

[![GitHub release (latest by date)](https://img.shields.io/github/v/release/nemoob/atlas-chain?style=flat-square)](https://github.com/nemoob/atlas-chain/releases)
[![GitHub stars](https://img.shields.io/github/stars/nemoob/atlas-chain?style=flat-square)](https://github.com/nemoob/atlas-chain/stargazers)
[![GitHub forks](https://img.shields.io/github/forks/nemoob/atlas-chain?style=flat-square)](https://github.com/nemoob/atlas-chain/network)
[![GitHub issues](https://img.shields.io/github/issues/nemoob/atlas-chain?style=flat-square)](https://github.com/nemoob/atlas-chain/issues)
[![GitHub license](https://img.shields.io/github/license/nemoob/atlas-chain?style=flat-square)](https://github.com/nemoob/atlas-chain/blob/main/LICENSE)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.nemoob/atlas-chain-core?style=flat-square)](https://search.maven.org/artifact/io.github.nemoob/atlas-chain-core)
[![Java Version](https://img.shields.io/badge/Java-8%2B-brightgreen?style=flat-square)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.2%2B-brightgreen?style=flat-square)](https://spring.io/projects/spring-boot)

一个基于Java的责任链框架，支持核心模块独立使用和Spring Boot集成。

## 📊 项目统计

### 基础统计
![GitHub repo size](https://img.shields.io/github/repo-size/nemoob/atlas-chain?style=flat-square)
![GitHub code size in bytes](https://img.shields.io/github/languages/code-size/nemoob/atlas-chain?style=flat-square)
![Lines of code](https://img.shields.io/tokei/lines/github/nemoob/atlas-chain?style=flat-square)
![GitHub language count](https://img.shields.io/github/languages/count/nemoob/atlas-chain?style=flat-square)
![GitHub top language](https://img.shields.io/github/languages/top/nemoob/atlas-chain?style=flat-square)

### 活跃度统计
![GitHub commit activity](https://img.shields.io/github/commit-activity/m/nemoob/atlas-chain?style=flat-square)
![GitHub commit activity](https://img.shields.io/github/commit-activity/w/nemoob/atlas-chain?style=flat-square)
![GitHub last commit](https://img.shields.io/github/last-commit/nemoob/atlas-chain?style=flat-square)
![GitHub contributors](https://img.shields.io/github/contributors/nemoob/atlas-chain?style=flat-square)

### 下载统计
![GitHub all releases](https://img.shields.io/github/downloads/nemoob/atlas-chain/total?style=flat-square)
![GitHub release (latest by date)](https://img.shields.io/github/downloads/nemoob/atlas-chain/latest/total?style=flat-square)

### 历史趋势
![GitHub Repo stars](https://img.shields.io/github/stars/nemoob/atlas-chain?style=social)
![GitHub watchers](https://img.shields.io/github/watchers/nemoob/atlas-chain?style=social)

**Star History**

[![Star History Chart](https://api.star-history.com/svg?repos=nemoob/atlas-chain&type=Date)](https://star-history.com/#nemoob/atlas-chain&Date)

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