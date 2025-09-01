package io.github.nemoob.atlas.chain.core.registry;

import io.github.nemoob.atlas.chain.core.handler.BaseHandler;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 责任链注册器，负责注册和管理处理者
 * @param <P> Param类型，表示请求参数
 * @param <R> Response类型，表示响应结果
 */
@Data
@NoArgsConstructor
public class ChainRegistry<P, R> {
    // 存储每个链ID对应的处理者列表
    private Map<String, List<BaseHandler<P, R>>> handlerMap = new HashMap<>();
    
    /**
     * 注册处理者
     * @param chainId 链ID
     * @param handler 处理者
     */
    public void registerHandler(String chainId, BaseHandler<P, R> handler) {
        handlerMap.computeIfAbsent(chainId, k -> new ArrayList<>()).add(handler);
    }
    
    /**
     * 构建指定链ID的责任链
     * @param chainId 链ID
     * @return 处理者列表
     */
    public List<BaseHandler<P, R>> buildChain(String chainId) {
        return handlerMap.getOrDefault(chainId, new ArrayList<>());
    }
    
    /**
     * 获取所有注册的链ID
     * @return 链ID集合
     */
    public Set<String> getAllChainIds() {
        return handlerMap.keySet();
    }
    
    /**
     * 清空所有注册的处理者
     */
    public void clear() {
        handlerMap.clear();
    }
}