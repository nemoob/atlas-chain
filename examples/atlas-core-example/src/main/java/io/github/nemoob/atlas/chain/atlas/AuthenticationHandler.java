package io.github.nemoob.atlas.chain.atlas;

import io.github.nemoob.atlas.chain.core.context.HandlerContext;
import io.github.nemoob.atlas.chain.core.handler.BaseHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * 认证处理者示例
 */
@Slf4j
public class AuthenticationHandler extends BaseHandler<UserRequest, UserResponse> {
    @Override
    protected boolean doHandle(HandlerContext<UserRequest, UserResponse> context) {
        // 执行认证逻辑
        UserRequest request = context.getRequest();
        log.info("Authentication check passed for user: {}", request.getUsername());
        
        // 可以在上下文中设置数据供后续处理者使用
        context.setAttribute(AuthContextKeys.USER_ID, "12345");
        context.setAttribute(AuthContextKeys.AUTH_RESULT, true);
        
        // 返回true表示继续执行下一个处理者
        return true;
    }
}