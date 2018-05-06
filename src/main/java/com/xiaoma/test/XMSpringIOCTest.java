package com.xiaoma.test;

import com.xiaoma.demo.mvc.action.DemoAction;
import com.xiaoma.spring.framework.context.XMApplicationContext;

import java.util.Arrays;

class A {
    public void print() {
        System.out.println("A");
        this.printA();
    }

    public void printA() {
        System.out.println("printA");

    }
}

class B extends A {
    public void print() {
        System.out.println("B");
        this.printA();
    }

    public void printA() {
        System.out.println("printB");

    }
}

public class XMSpringIOCTest {
    public static void main(String[] args) {
//        XMApplicationContext applicationContext = new XMApplicationContext("classpath:application.properties");
//        DemoAction action = (DemoAction)applicationContext.getBean("demoAction");
//        action.getDemoService().get("wuyiwen");
        A a = new B();
        a.print();
    }
}
