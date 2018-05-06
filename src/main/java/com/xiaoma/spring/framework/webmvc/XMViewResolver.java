package com.xiaoma.spring.framework.webmvc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//设计这个类的主要目的是：
/*
* 1.将一个静态文件变为一个动态文件
* 2.根据用户传送参数不同，产生不同的结果
* 最终输出字符串，交给Reponse输出
* */
public class XMViewResolver {

    private String viewName;

    private File templateFile;

    public XMViewResolver(String viewName, File templateFile) {
        this.viewName = viewName;
        this.templateFile = templateFile;
    }


    public String viewResolver(XMModelAndView mv) throws IOException {

        StringBuffer sb = new StringBuffer();

        RandomAccessFile ra = new RandomAccessFile(this.templateFile, "r");

        String line = null;

        while ((line = ra.readLine()) != null) {
            line = new String(line.getBytes("ISO-8859-1"), "utf-8");
            Matcher m = matcher(line);
            while (m.find()) {
                for (int i = 1; i <= m.groupCount(); i++) {
                    String paramName = m.group(i);
                    Object paramValue = mv.getModel().get(paramName);
                    if (paramValue == null) {
                        continue;
                    }

                    line = line.replaceAll("￥\\{" + paramName + "\\}", paramValue.toString());
                    line = new String(line.getBytes("utf-8"), "ISO-8859-1");
                }
            }

            sb.append(line);
        }
        return sb.toString();
    }

    private Matcher matcher(String str) {
        Pattern pattern = Pattern.compile("￥\\{(.+?)\\}", Pattern.CASE_INSENSITIVE);
        Matcher m = pattern.matcher(str);
        return m;
    }

    public String getViewName() {
        return viewName;
    }

    public void setViewName(String viewName) {
        this.viewName = viewName;
    }

    public File getTemplateFile() {
        return templateFile;
    }

    public void setTemplateFile(File templateFile) {
        this.templateFile = templateFile;
    }
}
