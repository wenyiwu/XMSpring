package com.xiaoma.spring.framework.context.support;

import com.xiaoma.spring.framework.beans.BeanDefinition;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

//用来对配置文件进行查找，读取，解析
public class BeanDefinitionReader {

    public static final String SCAN_PACKAGE = "scanPackage";

    private Properties config = new Properties();

    private List<String> registryBeanClasses = new ArrayList<String>();

    public BeanDefinitionReader(String ... locations) {
        //在Spring中是通过Reader去查找和定位
        //为了简便，默认读取第一个
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(locations[0].replace("classpath:", ""));

        try {
            config.load(is);
            doScanner(config.getProperty(SCAN_PACKAGE));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //递归扫描所有相关联的class，并且保存到list中
    private void doScanner(String pakName) {

        URL url = this.getClass().getClassLoader().getResource("/" + pakName.replaceAll("\\.", "/"));

        File classDir = new File(url.getFile());

        for (File file : classDir.listFiles()) {
            if (file.isDirectory()) {
                doScanner(pakName + "." + file.getName());
            } else {
                registryBeanClasses.add(pakName + "." + file.getName().replace(".class", ""));

            }
        }

    }

    public List<String> loadBeanDefinitions(){
        return this.registryBeanClasses;
    }

    //每注册一个className就返回一个BeanDefinition
    public BeanDefinition registerBean(String className){

        if(this.registryBeanClasses.contains(className)) {
            BeanDefinition beanDefinition = new BeanDefinition();

            beanDefinition.setBeanClassName(className);

            beanDefinition.setFactoryBeanName(lowerFirstCase(className.substring(className.lastIndexOf(".") + 1)));
            return beanDefinition;


        }

        return null;
    }

    public Properties getConfig(){
        return this.config;
    }


    private String lowerFirstCase(String str) {
        if (!str.isEmpty()) {
            char[] chars = str.toCharArray();

            int ascc = chars[0];
            if (ascc < 97) {
                chars[0] += 32;
            }
            return String.valueOf(chars);
        }
        return "";
    }
}
