package io.github.nemoob.atlas.chain.atlas;

import io.github.nemoob.atlas.chain.core.context.HandlerContext;
import io.github.nemoob.atlas.chain.core.executor.ChainExecutor;
import io.github.nemoob.atlas.chain.core.registry.ChainRegistry;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * 核心模块测试类
 */
public class CoreExampleTest {
    
    @Test
    public void testChainExecution() throws Exception {
        // 创建注册器
        ChainRegistry<UserRequest, UserResponse> registry = new ChainRegistry<>();
        
        // 创建处理者
        AuthenticationHandler authHandler = new AuthenticationHandler();
        AuthorizationHandler authzHandler = new AuthorizationHandler();
        BusinessLogicHandler businessHandler = new BusinessLogicHandler();
        
        // 注册处理者到指定链路
        String chainId = "test-chain";
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
        
        // 验证结果
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Operation completed successfully", response.getMessage());
        
        // 验证上下文属性
        assertEquals("12345", context.getAttribute(AuthContextKeys.USER_ID, String.class));
        assertEquals(true, context.getAttribute(AuthContextKeys.AUTH_RESULT, Boolean.class));
        
        // 关闭执行器
        executor.shutdown();
    }
}