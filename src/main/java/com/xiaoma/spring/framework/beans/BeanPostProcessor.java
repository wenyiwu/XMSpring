package com.xiaoma.spring.framework.beans;

//用于做时间监听的
public class BeanPostProcessor {

    public Object postProcessorBeforeInitialization(Object bean, String beanName) {
        return bean;
    }

    public Object postProcessorAfterInitialization(Object bean, String beanName) {
        return bean;
    }
}
