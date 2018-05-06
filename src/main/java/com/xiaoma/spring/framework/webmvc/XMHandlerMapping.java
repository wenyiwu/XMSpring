package com.xiaoma.spring.framework.webmvc;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

public class XMHandlerMapping {
    private Object controller;

    private Method method;

    //Url包装
    private Pattern url;

    public XMHandlerMapping(Pattern url, Object controller, Method method) {
        this.controller = controller;
        this.method = method;
        this.url = url;
    }

    public Object getController() {
        return controller;
    }

    public void setController(Object controller) {
        this.controller = controller;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Pattern getUrl() {
        return url;
    }

    public void setUrl(Pattern url) {
        this.url = url;
    }
}
