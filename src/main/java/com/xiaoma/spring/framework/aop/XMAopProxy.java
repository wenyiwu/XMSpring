package com.xiaoma.spring.framework.aop;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

//默认使用JDK动态代理
public class XMAopProxy implements InvocationHandler{
    private XMAopConfig config;

    private Object target;

    //把原生的对象传进来
    public Object getProxy(Object instance) {
        this.target = instance;

        Class<?> clazz = instance.getClass();

        return Proxy.newProxyInstance(clazz.getClassLoader(), clazz.getInterfaces(), this);
    }

    public void setConfig(XMAopConfig config) {
        this.config = config;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        Method m = this.target.getClass().getMethod(method.getName(), method.getParameterTypes());
//        m.invoke(this.target, args);
        //在原始方法调用之前
        if (config.contaions(m)) {
            XMAopConfig.XMAspect aspect = config.get(m);
            aspect.getPoints()[0].invoke(aspect.getAspect());
        }
        //反射调用原始方法
        Object obj = method.invoke(this.target, args);

        //在原始方法调用之后
        if (config.contaions(m)) {
            XMAopConfig.XMAspect aspect = config.get(m);
            aspect.getPoints()[1].invoke(aspect.getAspect());
        }

        return obj;
    }
}
