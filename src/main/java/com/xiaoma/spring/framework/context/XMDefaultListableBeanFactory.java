package com.xiaoma.spring.framework.context;

import com.xiaoma.spring.framework.beans.BeanDefinition;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class XMDefaultListableBeanFactory extends XMAbstractApplicationContext{

    //保存配置信息
    protected Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<String, BeanDefinition>();

    protected void onRefresh(){
    }

    @Override
    protected void refreshBeanFactory() {

    }


}
