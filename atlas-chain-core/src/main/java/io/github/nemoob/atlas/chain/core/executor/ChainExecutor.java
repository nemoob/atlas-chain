package io.github.nemoob.atlas.chain.core.executor;

import io.github.nemoob.atlas.chain.core.context.HandlerContext;
import io.github.nemoob.atlas.chain.core.handler.BaseHandler;
import io.github.nemoob.atlas.chain.core.registry.ChainRegistry;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 责任链执行器，负责执行责任链
 * @param <P> Param类型，表示请求参数
 * @param <R> Response类型，表示响应结果
 */
@Data
@AllArgsConstructor
public class ChainExecutor<P, R> {
    // 链注册器
    private ChainRegistry<P, R> chainRegistry;
    
    // 自定义线程池，用于异步执行
    private ExecutorService executorService = Executors.newFixedThreadPool(10);
    
    /**
     * 同步执行责任链
     * @param chainId 链ID
     * @param context 处理上下文
     * @return 响应结果
     * @throws Exception 执行异常
     */
    public R execute(String chainId, HandlerContext<P, R> context) throws Exception {
        List<BaseHandler<P, R>> handlers = chainRegistry.buildChain(chainId);
        
        // 同步执行责任链
        for (BaseHandler<P, R> handler : handlers) {
            try {
                // 判断是否跳过当前处理者
                if (handler.shouldSkip(context)) {
                    continue;
                }
                
                // 执行处理逻辑
                boolean shouldContinue = handler.doHandle(context);
                
                // 执行完成回调
                handler.onCompleted(context);
                
                // 判断是否继续执行下一个处理者
                if (!shouldContinue) {
                    break; // 明确返回false，中断责任链
                }
            } catch (Exception e) {
                // 执行错误回调
                handler.onError(context, e);
                // 异常会直接抛出到最上层，责任链中断执行
                throw e;
            }
        }
        
        return context.getResponse();
    }
    
    /**
     * 异步执行责任链，使用自定义线程池
     * @param chainId 链ID
     * @param context 处理上下文
     * @return CompletableFuture异步结果
     */
    public CompletableFuture<R> executeAsync(String chainId, HandlerContext<P, R> context) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return execute(chainId, context);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, executorService);
    }
    
    /**
     * 允许外部配置线程池
     * @param executorService 线程池
     */
    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }
    
    /**
     * 关闭线程池
     */
    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}