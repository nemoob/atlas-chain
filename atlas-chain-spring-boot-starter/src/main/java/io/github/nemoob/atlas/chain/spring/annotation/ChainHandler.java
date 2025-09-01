package io.github.nemoob.atlas.chain.spring.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * 责任链处理者注解，用于标记处理者类
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Component
public @interface ChainHandler {
    /**
     * 链的ID
     * @return 链ID
     */
    String value();
    
    /**
     * 处理者在链中的顺序，数值越小优先级越高
     * @return 顺序值
     */
    int order() default 0;
}