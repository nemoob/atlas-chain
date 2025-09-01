package io.github.nemoob.atlas.chain.spring.atlas;

import io.github.nemoob.atlas.chain.core.context.HandlerContext;
import io.github.nemoob.atlas.chain.core.handler.BaseHandler;
import io.github.nemoob.atlas.chain.spring.annotation.ChainHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 用户处理处理者示例
 */
@Slf4j
@ChainHandler(value = "user-process", order = 2)
@Component
public class UserProcessHandler extends BaseHandler<UserRequest, UserResponse> {
    @Override
    protected boolean doHandle(HandlerContext<UserRequest, UserResponse> context) {
        Boolean validated = context.getAttribute("validated", Boolean.class);
        if (validated != null && validated) {
            // 处理业务逻辑
            UserRequest request = context.getRequest();
            log.info("Processing user with userId: {}", request.getUserId());
            context.setResponse(new UserResponse("User processed successfully"));
        }
        return true;
    }
}