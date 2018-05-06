package com.xiaoma.spring.framework.aop;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

//只是对application中的expression进行封装
//目标代理对象的一个方法，要增强
//由用户自己实现的业务逻辑去增强
//配置文件的目的：告诉Spring，哪些类的哪些方法需要增强，增强的内容是什么
//对配置文件中所体现的内容进行封装
public class XMAopConfig {

    //以目标对象的Method作为key，需要增强的代码内容作为value
    private Map<Method, XMAspect> points = new HashMap<Method, XMAspect>();

    public void put(Method target, Object aspect, Method[] points) {
        this.points.put(target, new XMAspect(aspect, points));
    }

    public XMAspect get(Method method) {
        return this.points.get(method);
    }

    public boolean contaions(Method method) {
        return this.points.containsKey(method);
    }

    //对增强的代码的封装
    public class XMAspect{
        private Object aspect;      //将LogAspect这个对象赋值给它
        private Method[] points;    //会将LogAspect的before和after方法赋值进来

        public XMAspect(Object aspect, Method[] points) {
            this.aspect = aspect;
            this.points = points;
        }


        public Object getAspect() {
            return aspect;
        }

        public void setAspect(Object aspect) {
            this.aspect = aspect;
        }

        public Method[] getPoints() {
            return points;
        }

        public void setPoints(Method[] points) {
            this.points = points;
        }
    }

}
