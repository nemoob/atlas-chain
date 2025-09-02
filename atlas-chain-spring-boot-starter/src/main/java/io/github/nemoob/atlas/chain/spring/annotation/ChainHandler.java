package io.github.nemoob.atlas.chain.spring.annotation;

import java.lang.annotation.*;

/**
 * 责任链处理者注解，用于标记处理者类
 * 注意：使用此注解的类还需要添加 @Component 注解才能被Spring管理
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
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