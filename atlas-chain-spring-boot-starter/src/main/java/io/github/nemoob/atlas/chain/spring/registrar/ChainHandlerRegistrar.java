package io.github.nemoob.atlas.chain.spring.registrar;

import io.github.nemoob.atlas.chain.core.handler.BaseHandler;
import io.github.nemoob.atlas.chain.core.registry.ChainRegistry;
import io.github.nemoob.atlas.chain.spring.annotation.ChainHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 责任链处理者注册器，负责扫描并注册带有@ChainHandler注解的处理者
 */
@Slf4j
@Component
public class ChainHandlerRegistrar implements BeanPostProcessor, ApplicationContextAware {
    private ApplicationContext applicationContext;
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
    
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        // 检查bean是否有@ChainHandler注解
        ChainHandler chainHandler = AnnotationUtils.findAnnotation(bean.getClass(), ChainHandler.class);
        if (chainHandler != null && bean instanceof BaseHandler) {
            // 获取链注册器
            Map<String, ChainRegistry> registryBeans = applicationContext.getBeansOfType(ChainRegistry.class);
            if (!registryBeans.isEmpty()) {
                ChainRegistry registry = registryBeans.values().iterator().next();
                
                // 注册处理者
                String chainId = chainHandler.value();
                registry.registerHandler(chainId, (BaseHandler) bean);
                
                log.info("Registered handler {} for chain {}", bean.getClass().getSimpleName(), chainId);
            }
        }
        return bean;
    }
}