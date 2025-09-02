package io.github.nemoob.atlas.chain.spring.registrar;

import io.github.nemoob.atlas.chain.core.handler.BaseHandler;
import io.github.nemoob.atlas.chain.core.registry.ChainRegistry;
import io.github.nemoob.atlas.chain.spring.annotation.ChainHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 责任链处理者注册器，负责扫描并注册带有@ChainHandler注解的处理者
 */
@Slf4j
@Component
public class ChainHandlerRegistrar implements BeanPostProcessor, ApplicationContextAware {
    private ApplicationContext applicationContext;
    private final List<HandlerInfo> handlerInfos = new ArrayList<>();
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
    
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        // 检查bean是否有@ChainHandler注解
        ChainHandler chainHandler = AnnotationUtils.findAnnotation(bean.getClass(), ChainHandler.class);
        if (chainHandler != null && bean instanceof BaseHandler) {
            // 收集处理器信息，稍后统一排序注册
            handlerInfos.add(new HandlerInfo(
                chainHandler.value(),
                chainHandler.order(),
                (BaseHandler) bean
            ));
            log.debug("Collected handler {} for chain {} with order {}", 
                bean.getClass().getSimpleName(), chainHandler.value(), chainHandler.order());
        }
        return bean;
    }
    
    /**
     * 在Spring容器刷新完成后，按order排序并注册所有处理器
     */
    @EventListener(ContextRefreshedEvent.class)
    public void registerHandlersAfterContextRefresh() {
        if (handlerInfos.isEmpty()) {
            return;
        }
        
        // 获取链注册器
        Map<String, ChainRegistry> registryBeans = applicationContext.getBeansOfType(ChainRegistry.class);
        if (registryBeans.isEmpty()) {
            log.warn("No ChainRegistry found, handlers will not be registered");
            return;
        }
        
        ChainRegistry registry = registryBeans.values().iterator().next();
        
        // 按链ID分组，然后按order排序
        Map<String, List<HandlerInfo>> chainGroups = handlerInfos.stream()
            .collect(Collectors.groupingBy(HandlerInfo::getChainId));
        
        for (Map.Entry<String, List<HandlerInfo>> entry : chainGroups.entrySet()) {
            String chainId = entry.getKey();
            List<HandlerInfo> handlers = entry.getValue();
            
            // 按order排序（数值越小优先级越高）
            handlers.sort(Comparator.comparingInt(HandlerInfo::getOrder));
            
            // 按顺序注册处理器
            for (HandlerInfo handlerInfo : handlers) {
                registry.registerHandler(chainId, handlerInfo.getHandler());
                log.info("Registered handler {} for chain {} with order {}", 
                    handlerInfo.getHandler().getClass().getSimpleName(), 
                    chainId, 
                    handlerInfo.getOrder());
            }
        }
        
        // 清空临时存储
        handlerInfos.clear();
    }
    
    /**
     * 处理器信息内部类
     */
    private static class HandlerInfo {
        private final String chainId;
        private final int order;
        private final BaseHandler handler;
        
        public HandlerInfo(String chainId, int order, BaseHandler handler) {
            this.chainId = chainId;
            this.order = order;
            this.handler = handler;
        }
        
        public String getChainId() {
            return chainId;
        }
        
        public int getOrder() {
            return order;
        }
        
        public BaseHandler getHandler() {
            return handler;
        }
    }
}