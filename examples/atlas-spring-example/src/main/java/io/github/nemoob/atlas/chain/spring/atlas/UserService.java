package io.github.nemoob.atlas.chain.spring.atlas;

import io.github.nemoob.atlas.chain.core.context.HandlerContext;
import io.github.nemoob.atlas.chain.core.executor.ChainExecutor;
import io.github.nemoob.atlas.chain.core.registry.ChainRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 用户服务类
 */
@Slf4j
@Service
public class UserService {
    
    @Autowired
    private ChainExecutor<UserRequest, UserResponse> chainExecutor;
    
    @Autowired
    private ChainRegistry<UserRequest, UserResponse> chainRegistry;
    
    /**
     * 处理用户请求
     * @param userId 用户ID
     * @return 用户响应
     */
    public UserResponse processUser(Long userId) {
        try {
            UserRequest request = new UserRequest(userId, "testUser");
            HandlerContext<UserRequest, UserResponse> context = new HandlerContext<>(request, null);
            return chainExecutor.execute("user-process", context);
        } catch (Exception e) {
            log.error("Error processing user: {}", e.getMessage(), e);
            return new UserResponse("Error processing user: " + e.getMessage());
        }
    }
}