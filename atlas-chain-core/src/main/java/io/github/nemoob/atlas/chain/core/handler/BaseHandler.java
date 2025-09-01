package io.github.nemoob.atlas.chain.core.handler;

import io.github.nemoob.atlas.chain.core.context.HandlerContext;
import lombok.extern.slf4j.Slf4j;

/**
 * 责任链处理者基类
 * @param <P> Param类型，表示请求参数
 * @param <R> Response类型，表示响应结果
 */
@Slf4j
public abstract class BaseHandler<P, R> {
    /**
     * 子类必须实现的业务逻辑方法
     * 返回true表示继续执行下一个处理者，false表示中断链，不再执行后续节点
     * 注意：此方法必须明确返回true或false，无默认值
     * @param context 处理上下文
     * @return true继续执行，false中断执行
     */
    public abstract boolean doHandle(HandlerContext<P, R> context);
    
    /**
     * 可选实现：判断是否跳过当前处理者，默认不跳过
     * @param context 处理上下文
     * @return true跳过当前处理者，false不跳过
     */
    public boolean shouldSkip(HandlerContext<P, R> context) {
        return false;
    }
    
    /**
     * 可选实现：异步回调方法，在处理完成后执行
     * @param context 处理上下文
     */
    public void onCompleted(HandlerContext<P, R> context) {
        // 默认空实现
        log.debug("Handler completed: {}", this.getClass().getSimpleName());
    }
    
    /**
     * 可选实现：异步回调方法，在处理出错时执行
     * @param context 处理上下文
     * @param e 异常信息
     */
    public void onError(HandlerContext<P, R> context, Exception e) {
        // 默认空实现
        log.error("Handler error in {}: {}", this.getClass().getSimpleName(), e.getMessage(), e);
    }
}