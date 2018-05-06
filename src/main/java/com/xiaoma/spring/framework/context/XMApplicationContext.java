package com.xiaoma.spring.framework.context;

import com.xiaoma.spring.framework.annotation.Autowired;
import com.xiaoma.spring.framework.annotation.Controller;
import com.xiaoma.spring.framework.annotation.Service;
import com.xiaoma.spring.framework.aop.XMAopConfig;
import com.xiaoma.spring.framework.beans.BeanDefinition;
import com.xiaoma.spring.framework.beans.BeanPostProcessor;
import com.xiaoma.spring.framework.beans.BeanWrapper;
import com.xiaoma.spring.framework.context.support.BeanDefinitionReader;
import com.xiaoma.spring.framework.core.BeanFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class XMApplicationContext extends XMDefaultListableBeanFactory implements BeanFactory {

    private String[] configLocations;

    private BeanDefinitionReader reader;


    //用来保证注册式单例的容器
    private Map<String, Object> beanCacheMap = new ConcurrentHashMap<String, Object>();

    //用来存储所有的被代理过的对象
    private Map<String, BeanWrapper> beanWrapperMap = new ConcurrentHashMap<String, BeanWrapper>();

    public XMApplicationContext(String... configLocations) {
        this.configLocations = configLocations;
        refresh();
    }

    public void refresh() {
        //1.定位
        this.reader = new BeanDefinitionReader(configLocations);

        //2.加载
        List<String> beanDefinitions = reader.loadBeanDefinitions();

        //3.注册
        doRegistry(beanDefinitions);

        //4.依赖注入(lazy-init=false)
        //在这里自动调用getBean方法
        doAutowrited();


    }

    //开始执行自动化的依赖注入
    private void doAutowrited() {
        beanDefinitionMap.forEach((beanName, beanDefinition) -> {
            if (!beanDefinition.isLazyInit()) {
                getBean(beanName);
            }
        });

        beanWrapperMap.forEach((beanName, wrapper) -> {
            populateBean(beanName, wrapper.getOriginalInstance());

        });
    }

    public void populateBean(String beanName, Object instance) {

        Class clazz = instance.getClass();

        if (!(clazz.isAnnotationPresent(Controller.class) || clazz.isAnnotationPresent(Service.class))) {
            return;
        }

        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            if (!field.isAnnotationPresent(Autowired.class)) {
                continue;
            }

            Autowired autowired = field.getAnnotation(Autowired.class);

            String autowritedBeanName = autowired.value().trim();

            if ("".equals(autowritedBeanName)) {
                autowritedBeanName = field.getType().getName();
            }

            field.setAccessible(true);

            try {
                //System.out.println("=======================" +instance +"," + autowritedBeanName + "," + this.beanWrapperMap.get(autowritedBeanName).getWrappedInstance().getClass().getName());
                field.set(instance, this.beanWrapperMap.get(autowritedBeanName).getWrappedInstance());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    //真正的将BeanDefinitions注册到IOC容器中
    private void doRegistry(List<String> beanDefinitions) {

        try {
            for (String className : beanDefinitions) {

                Class<?> beanClass = Class.forName(className);

                //如果是一个接口，是不能实例化的
                //用它实现类来实例化
                if (beanClass.isInterface()) {
                    continue;
                }

                BeanDefinition beanDefinition = reader.registerBean(className);
                if (beanDefinition != null) {
                    this.beanDefinitionMap.put(beanDefinition.getFactoryBeanName(), beanDefinition);
                }

                Class<?>[] interfaces = beanClass.getInterfaces();
                for (Class<?> i : interfaces) {
                    //如果是多个实现类，只能覆盖
                    //因为Spring没有那么智能，只能覆盖
                    //这个时候可以自定义名字
                    this.beanDefinitionMap.put(i.getName(), beanDefinition);
                }


                //到这里为止，容器初始化完毕


                //beanName有三种情况
                //1.默认是类名首字母小写
                //2.自定义
                //3.接口注入
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    //依赖注入，从这里开始
    //通过读取BeanDefinition中的信息
    //然后，通过反射机制创建一个实例，并返回
    //Spring的做法是不会把最原始的对象放出去，会用一个BeanWrapper来进行一次包装
    //装饰器模式：
    //1.保留原来的OOP关系
    //2.我需要对它进行扩展，增强（为了以后的AOP打基础）
    //
    @Override
    public Object getBean(String beanName) {

        BeanDefinition beanDefinition = this.beanDefinitionMap.get(beanName);

        String className = beanDefinition.getBeanClassName();

        try {

            //生成通知事件
            BeanPostProcessor beanPostProcessor = new BeanPostProcessor();


            Object instance = instantionBean(beanDefinition);
            if (instance == null) {
                return null;
            }
            //在实例初始化以前调用一次
            beanPostProcessor.postProcessorBeforeInitialization(instance, beanName);


            BeanWrapper beanWrapper = new BeanWrapper(instance);
            beanWrapper.setAopConfig(instantionAopConfig(beanDefinition));
            beanWrapper.setPostProcessor(beanPostProcessor);
            this.beanWrapperMap.put(beanName, beanWrapper);

            //在实例初始化以后调用一次
            beanPostProcessor.postProcessorAfterInitialization(instance, beanName);

//            populateBean(beanName, beanWrapper.getOriginalInstance());

            //通过这样一调用，相当于给我们自己留有了可操作的空间
            return this.beanWrapperMap.get(beanName).getWrappedInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    //传一个BeanDefinition，就返回一个实例Bean
    private Object instantionBean(BeanDefinition beanDefinition) {

        Object instance = null;

        String className = beanDefinition.getBeanClassName();

        try {

            //根据class才能确定一个类是否有实例
            if (this.beanCacheMap.containsKey(className)) {
                instance = this.beanCacheMap.get(className);
            } else {
                Class<?> clazz = Class.forName(className);
                instance = clazz.newInstance();
                this.beanCacheMap.put(className, instance);
            }
            return instance;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }


    private XMAopConfig instantionAopConfig(BeanDefinition beanDefinition) throws Exception {
        XMAopConfig config = new XMAopConfig();

        String expression = reader.getConfig().getProperty("pointCut");
        String[] before = reader.getConfig().getProperty("aspextBefore").split("\\s");
        String[] after = reader.getConfig().getProperty("aspextAfter").split("\\s");

        String className = beanDefinition.getBeanClassName();

        Class<?> clazz = Class.forName(className);

        Pattern pattern = Pattern.compile(expression);

        Class<?> aspextClass = Class.forName(before[0]);

        for (Method m : clazz.getMethods()) {
            Matcher matcher = pattern.matcher(m.toString());

            if (matcher.matches()) {
                config.put(m, aspextClass.newInstance(), new Method[]{aspextClass.getMethod(before[1]), aspextClass.getMethod(after[1])});
            }
        }

        return config;
    }

    public String[] getBeanDefinitionNames() {
        return this.beanDefinitionMap.keySet().toArray(new String[this.beanDefinitionMap.size()]);
    }

    public int getBeanDefinitionCount() {
        return beanDefinitionMap.size();
    }

    public Properties getConfig() {
        return this.reader.getConfig();
    }

}
