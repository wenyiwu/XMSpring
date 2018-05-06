package com.xiaoma.spring.framework.context;

public abstract class XMAbstractApplicationContext {

    //提供给子类
    protected void onRefresh(){

    }

    protected  abstract void refreshBeanFactory();

}
