package com.xiaoma.spring.framework.webmvc;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Map;

public class XMHandlerAdapter {
    private Map<String, Integer> paramMapping;


    public XMHandlerAdapter(Map<String, Integer> paramMapping) {
        this.paramMapping = paramMapping;
    }

    //根据用户请求的参数信息，跟Method中的参数信息进行动态匹配
    //resp 传进来的目的只有一个：只是为了将其赋值给方法参数，仅此而已
    public XMModelAndView handler(HttpServletRequest req, HttpServletResponse resp, XMHandlerMapping handler) throws InvocationTargetException, IllegalAccessException {

        //只有当用户传过来的 ModelAndView 是空的时候，才会new一个默认的

        //1.调用之前要准备好这个方法的形参
        Class<?>[] paramTypes = handler.getMethod().getParameterTypes();

        //2.拿到自定义的命名参数所在的位置
        //用户通过yrl传过来的参数列表
        Map<String, String[]> reqParamValues = req.getParameterMap();

        //3.构造实参列表
        Object[] paramValues = new Object[paramTypes.length];
        reqParamValues.forEach((k, v) -> {
            String value = Arrays.toString(v).replaceAll("\\[|\\]", "").replaceAll("\\s", ",");

            if (!this.paramMapping.containsKey(k)) {
                return;
            }

            int index = this.paramMapping.get(k);

            paramValues[index] = caseStringValue(value, paramTypes[index]);
            //要针对我们传过来的参数进行类型转换
        });

        if (paramMapping.containsKey(HttpServletRequest.class.getName())) {
            int reqIndex = this.paramMapping.get(HttpServletRequest.class.getName());
            paramValues[reqIndex] = req;
        }
        if (paramMapping.containsKey(HttpServletResponse.class.getName())) {
            int respIndex = this.paramMapping.get(HttpServletResponse.class.getName());
            paramValues[respIndex] = resp;
        }

        //4.从handler中取出controller，method，然后利用反射机制进行调用

        Object result = handler.getMethod().invoke(handler.getController(), paramValues);
        boolean isMV = handler.getMethod().getReturnType() == XMModelAndView.class;

        if (isMV && result != null) {
            return (XMModelAndView) result;
        } else {
            return null;
        }
    }

    private Object caseStringValue(String value, Class<?> clazz) {
        if (clazz == String.class) {
            return value;
        } else if (clazz == Integer.class) {
            return Integer.valueOf(value);
        } else if (clazz == int.class) {
            return Integer.valueOf(value).intValue();
        }
        return null;
    }
}
