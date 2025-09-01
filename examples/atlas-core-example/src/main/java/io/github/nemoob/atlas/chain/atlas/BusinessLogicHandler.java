package io.github.nemoob.atlas.chain.atlas;

import io.github.nemoob.atlas.chain.core.context.HandlerContext;
import io.github.nemoob.atlas.chain.core.handler.BaseHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * 业务逻辑处理者示例
 */
@Slf4j
public class BusinessLogicHandler extends BaseHandler<UserRequest, UserResponse> {
    @Override
    protected boolean doHandle(HandlerContext<UserRequest, UserResponse> context) {
        // 执行业务逻辑
        log.info("Business logic executed");
        
        // 设置响应结果
        context.setResponse(new UserResponse("Operation completed successfully"));
        
        // 返回true表示继续执行下一个处理者，返回false则中断链的执行
        return true;
    }
}