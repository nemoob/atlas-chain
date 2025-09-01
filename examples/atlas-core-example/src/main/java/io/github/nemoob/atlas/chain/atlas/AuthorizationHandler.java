package io.github.nemoob.atlas.chain.atlas;

import io.github.nemoob.atlas.chain.core.context.HandlerContext;
import io.github.nemoob.atlas.chain.core.handler.BaseHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * 授权处理者示例
 */
@Slf4j
public class AuthorizationHandler extends BaseHandler<UserRequest, UserResponse> {
    @Override
    protected boolean doHandle(HandlerContext<UserRequest, UserResponse> context) {
        // 执行授权逻辑
        String userId = context.getAttribute(AuthContextKeys.USER_ID, String.class);
        Boolean authResult = context.getAttribute(AuthContextKeys.AUTH_RESULT, Boolean.class);
        
        if (authResult != null && authResult) {
            log.info("Authorization check passed for user: {}", userId);
        }
        
        // 返回true表示继续执行下一个处理者
        return true;
    }
}