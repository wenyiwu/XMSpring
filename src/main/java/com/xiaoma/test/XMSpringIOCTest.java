package com.xiaoma.test;

import com.xiaoma.demo.mvc.action.DemoAction;
import com.xiaoma.spring.framework.context.XMApplicationContext;

public class XMSpringIOCTest {
    public static void main(String[] args) {
        XMApplicationContext applicationContext = new XMApplicationContext("classpath:application.properties");
        DemoAction action = (DemoAction)applicationContext.getBean("demoAction");
        action.getDemoService().get("wuyiwen");
    }
}
