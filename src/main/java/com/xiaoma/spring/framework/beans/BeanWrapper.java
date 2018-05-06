package com.xiaoma.spring.framework.beans;

import com.xiaoma.spring.framework.aop.XMAopConfig;
import com.xiaoma.spring.framework.aop.XMAopProxy;
import com.xiaoma.spring.framework.core.FactoryBean;

public class BeanWrapper extends FactoryBean{

    //还会用到 观察者模式
    //1.支持事件响应，会有一个监听
    private BeanPostProcessor postProcessor;

    private Object wrapperInstance;

    //通过反射new出来的，要把包装起来，存下来
    private Object originalInstance;

    private XMAopProxy aopProxy = new XMAopProxy();

    public BeanWrapper(Object instance) {
        this.wrapperInstance = aopProxy.getProxy(instance);
        this.originalInstance = instance;
    }

    public Object getWrappedInstance() {
        return this.wrapperInstance;
    }

    //返回代理以后的Class
    //可能会是这个 $Proxy0
    public Class<?> getWrappedClass() {
        return this.wrapperInstance.getClass();
    }


    public BeanPostProcessor getPostProcessor() {
        return postProcessor;
    }

    public void setPostProcessor(BeanPostProcessor postProcessor) {
        this.postProcessor = postProcessor;
    }

    public void setAopConfig(XMAopConfig config){
        this.aopProxy.setConfig(config);
    }


    public Object getOriginalInstance() {
        return originalInstance;
    }

    public void setOriginalInstance(Object originalInstance) {
        this.originalInstance = originalInstance;
    }
}