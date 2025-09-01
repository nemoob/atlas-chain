package io.github.nemoob.atlas.chain.spring.atlas;

import io.github.nemoob.atlas.chain.core.context.HandlerContext;
import io.github.nemoob.atlas.chain.core.handler.BaseHandler;
import io.github.nemoob.atlas.chain.spring.annotation.ChainHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 用户验证处理者示例
 */
@Slf4j
@ChainHandler(value = "user-process", order = 1)
@Component
public class UserValidateHandler extends BaseHandler<UserRequest, UserResponse> {
    @Override
    protected boolean doHandle(HandlerContext<UserRequest, UserResponse> context) {
        UserRequest request = context.getRequest();
        if (request.getUserId() == null) {
            log.warn("User validation failed: userId is null");
            context.setResponse(new UserResponse("User validation failed"));
            return false; // 验证失败，终止链路
        }
        context.setAttribute("validated", true);
        log.info("User validation passed for userId: {}", request.getUserId());
        return true;
    }
}