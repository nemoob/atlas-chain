package io.github.nemoob.atlas.chain.spring.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 责任链处理者配置属性类
 */
@Data
@ConfigurationProperties(prefix = "chain.handler")
public class ChainHandlerProperties {
    private int corePoolSize = 5;
    private int maxPoolSize = 10;
    private int keepAliveTime = 60;
    private int queueCapacity = 100;
}