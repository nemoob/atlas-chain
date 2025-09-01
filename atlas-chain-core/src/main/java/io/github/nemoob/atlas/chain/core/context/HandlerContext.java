package io.github.nemoob.atlas.chain.core.context;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * 责任链上下文类，用于在处理者之间传递数据
 * @param <P> Param类型，表示请求参数
 * @param <R> Response类型，表示响应结果
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class HandlerContext<P, R> {
    // 请求参数
    private P request;
    // 响应结果
    private R response;
    // attributes用于在责任链的不同节点之间传递数据
    // 比如一个节点处理的结果可以被后续节点使用
    // 使用Object类型以支持不同数据类型，但建议通过定义常量类来约束键名
    private Map<String, Object> attributes = new HashMap<>();
    
    /**
     * 提供类型安全的属性获取方法
     * @param key 属性键
     * @param type 属性值类型
     * @param <V> 泛型类型
     * @return 指定类型的属性值
     */
    @SuppressWarnings("unchecked")
    public <V> V getAttribute(String key, Class<V> type) {
        return (V) attributes.get(key);
    }
    
    /**
     * 设置属性值
     * @param key 属性键
     * @param value 属性值
     */
    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }
    
    /**
     * 移除属性
     * @param key 属性键
     * @return 被移除的属性值
     */
    public Object removeAttribute(String key) {
        return attributes.remove(key);
    }
    
    // 约束attributes键名的方式：
    // 1. 建议为每个业务领域定义常量类，例如：
    // public class AuthContextKeys {
    //     public static final String USER_ID = "userId";
    //     public static final String USER_ROLE = "userRole";
    // }
    // 2. 使用时：context.setAttribute(AuthContextKeys.USER_ID, "12345");
}