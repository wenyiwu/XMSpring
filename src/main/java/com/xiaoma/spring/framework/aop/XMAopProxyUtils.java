package com.xiaoma.spring.framework.aop;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;

public class XMAopProxyUtils {

    public static Object getTargetObject(Object proxy) throws Exception {
        //先判断传进来的这个对象是不是一个代理过的对象

        if (!isAopProxy(proxy)) {
            return proxy;
        }

        return getProxyTargetObject(proxy);
    }

    private static boolean isAopProxy(Object object) {
        return Proxy.isProxyClass(object.getClass());
    }

    private static Object getProxyTargetObject(Object proxy) throws Exception {
        Field h = proxy.getClass().getSuperclass().getDeclaredField("h");

        h.setAccessible(true);
        XMAopProxy aopProxy = (XMAopProxy) h.get(proxy);

        Field target = aopProxy.getClass().getDeclaredField("target");

        target.setAccessible(true);

        return target.get(aopProxy);
    }

}
