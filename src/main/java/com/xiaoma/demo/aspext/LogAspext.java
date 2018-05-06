package com.xiaoma.demo.aspext;

public class LogAspext {

    //在调用一个方法之前执行
    public void before() {
        System.out.println("==================before=============");
    }

    //在调用一个方法之后执行
    public void after() {
        System.out.println("==================after=============");

    }

}
