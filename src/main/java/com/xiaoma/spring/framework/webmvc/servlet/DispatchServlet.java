package com.xiaoma.spring.framework.webmvc.servlet;

import com.xiaoma.spring.framework.annotation.*;
import com.xiaoma.spring.framework.aop.XMAopProxyUtils;
import com.xiaoma.spring.framework.context.XMApplicationContext;
import com.xiaoma.spring.framework.webmvc.XMModelAndView;
import com.xiaoma.spring.framework.webmvc.XMHandlerAdapter;
import com.xiaoma.spring.framework.webmvc.XMHandlerMapping;
import com.xiaoma.spring.framework.webmvc.XMViewResolver;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DispatchServlet extends HttpServlet {

    public static final String CONTEXT_CONFIG_LOCATION = "ContextConfigLocation";
    private Properties contextConfig = new Properties();

    //IOC容器
    private Map<String, Object> beanMap = new ConcurrentHashMap<String, Object>();

    private List<String> classNames = new ArrayList<String>();

    //SpringMVC中最核心，最经典的设计
    private List<XMHandlerMapping> handlerMappings = new ArrayList<XMHandlerMapping>();

    private Map<XMHandlerMapping, XMHandlerAdapter> handlerAdapters = new HashMap<>();

    private List<XMViewResolver> viewResolvers = new ArrayList<>();

    public void initV1(ServletConfig config) throws ServletException {
        //开始初始化

        //定位
        doLoadConfig(config.getInitParameter("ContextConfigLocation"));

        //加载
        doScanner(contextConfig.getProperty("scanPackage"));

        //注册
        doRegistry();

        //自动依赖注入
        //在Spring中是通过调用getBean方法才触发依赖注入的
        doAutowrite();

        //如果是SpringMVC会多设计一个HandlerMapping
        //将@RequestMapping中配置的url和一个Method关联上
        //以便于从浏览器获得用户输入的url以后，能够找到具体执行的Method，通过反射去调用
        initHandlerMapping();

        //
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        //相当于把IOC容器初始化了
        XMApplicationContext context = new XMApplicationContext(config.getInitParameter(CONTEXT_CONFIG_LOCATION));

        initStrategies(context);
    }

    protected void initStrategies(XMApplicationContext context) {
        //有九种策略
        //针对每个用户请求，都会经过一些处理的策略之后，最终才能有结果输出
        //每种策略可以自定义干预。但是最终的结果都是一致

        //这里就是ch传说中的九大组件
        initMultipartResolver(context);                //文件上传解析
        initLocaleResolver(context);                //本地化解析
        initThemeResolver(context);                    //主题解析

        //自己实现，用来保存Controller中配置的RequstMapping和Method的一个对应关系
        initHandlerMappings(context);                //通过HandlerMapping，将请求映射到处理器

        //自己实现，用来动态匹配Method参数，包括类转换，动态赋值
        initHandlerAdapters(context);                //通过HandlerMapping进行多参数类型的参数匹配
        initHandlerExceptionResolvers(context);        //如果执行中遇到异常，将交给HandlerException来处理
        initRequestToViewNameTranslator(context);    //直接解析请求到视图名

        //自己实现，通过viewResolvers实现动态模板的解析，自己解析一套模板语言
        initViewResolvers(context);                    //通过viewResolver解析逻辑视图到具体视图实现
        initFlashMapManager(context);                //flash映射管理器
    }

    //将Controller中配置的RequestMapping和Method进行一一对应
    private void initHandlerMappings(XMApplicationContext context) {
        //需要知道哪些类是需要handlerMapping的
        try {
            //首先从容器中取到所有的实例
            String[] beanNames = context.getBeanDefinitionNames();
            for (String beanName : beanNames) {
                //Object instance = context.getBean(beanName);
                //到了MVC层。对外提供的方法只有一个getBean方法
                //返回的对象不是BeanWrapper
                Object proxy = context.getBean(beanName);

                Object controller = XMAopProxyUtils.getTargetObject(proxy);

                Class<?> clazz = controller.getClass();
                if (!clazz.isAnnotationPresent(Controller.class)) {
                    continue;
                }

                String baseUrl = "";

                if (clazz.isAnnotationPresent(RequestMapping.class)) {
                    RequestMapping requestMapping = clazz.getAnnotation(RequestMapping.class);
                    baseUrl = requestMapping.value();
                }

                //扫描所有public方法
                Method[] methods = clazz.getMethods();
                for (Method method : methods) {
                    if (!method.isAnnotationPresent(RequestMapping.class)) {
                        continue;
                    }

                    RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);

                    if (baseUrl.equals("/")) {
                        baseUrl = "";
                    }
                    String regex = (baseUrl + requestMapping.value().replaceAll("\\*", ".*").replaceAll("/+", "/"));
                    Pattern pattern = Pattern.compile(regex);

                    this.handlerMappings.add(new XMHandlerMapping(pattern, controller, method));

                    System.out.println("Mapping " + regex + " , " + method);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //把所有方法的参数进行动态配置
    private void initHandlerAdapters(XMApplicationContext context) {
        //在初始化阶段，能做的就是讲这些参数的名字或者类型按一定的顺序保存下来
        //之后用反射调用的时候，传的形参是一个数组
        //可以通过记录这些参数的位置index，挨个从数组中填充，这样，就和参数的顺序无关

        for (XMHandlerMapping handlerMapping : this.handlerMappings) {
            //每一个方法有一个参数列表，那么这里保存的是形参列表

            //处理命名参数
            Map<String, Integer> paramMapping = new HashMap<String, Integer>();
            Annotation[][] pa = handlerMapping.getMethod().getParameterAnnotations();
            for (int i = 0; i < pa.length; i++) {
                for (Annotation a : pa[i]) {
                    if (a instanceof RequestParam) {
                        String paramName = ((RequestParam) a).value();
                        if (!"".equals(paramName.trim())) {
                            //之所以要记录参数的下标，是因为可能有没有进行注解的参数，那么应该把它置为空
                            paramMapping.put(paramName, i);
                        }
                    }
                }
            }

            //处理非命名参数
            Class<?>[] paramTypes = handlerMapping.getMethod().getParameterTypes();
            for (int i = 0; i < paramTypes.length; i++) {
                Class<?> type = paramTypes[i];
                if (type == HttpServletRequest.class || type == HttpServletResponse.class) {
                    paramMapping.put(type.getName(), i);
                }
            }

            this.handlerAdapters.put(handlerMapping, new XMHandlerAdapter(paramMapping));
        }
    }

    private void initFlashMapManager(XMApplicationContext context) {
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            doDispatch(req, resp);
        } catch (Exception e) {
            resp.getWriter().write("500 Exception, Details : \r\n" + Arrays.toString(e.getStackTrace()).replaceAll("\\[|\\]", "").replaceAll("\\s", "\r\n"));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("--------------------DO POST-------------");
        try {
            doDispatch(req, resp);
        } catch (Exception e) {
            resp.getWriter().write("500 Exception, Details : \r\n" + Arrays.toString(e.getStackTrace()).replaceAll("\\[|\\]", "").replaceAll("\\s", "\r\n"));
        }
    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        //根据用户请求的URL来获得一个Handler
        XMHandlerMapping handler = getHandler(req);
        if (handler == null) {
            resp.getWriter().write("404 Not found\r\n");
        }

        XMHandlerAdapter ha = getHandlerAdapter(handler);

        //这一步只是调用方法，得到返回值
        XMModelAndView mv = ha.handler(req, resp, handler);

        //这一步才是真正的输出
        processDispatchResult(resp, mv);

    }

    private void initViewResolvers(XMApplicationContext context) {
        //解决一个页面名字和模板文件关联的问题
        String templateRoot = context.getConfig().getProperty("templateRoot");
        String templateRootPath = this.getClass().getClassLoader().getResource(templateRoot).getFile();

        File templateRootDir = new File(templateRootPath);

        for (File template : templateRootDir.listFiles()) {
            this.viewResolvers.add(new XMViewResolver(template.getName(), template));
        }
    }

    private void processDispatchResult(HttpServletResponse resp, XMModelAndView mv) throws IOException {
        //调用viewResolver的resolverView方法
        if (mv == null) {
            return;
        }

        if (this.viewResolvers.isEmpty()) {
            return;
        }

        for (XMViewResolver viewResolver : this.viewResolvers) {
            if (mv.getViewName().equals(viewResolver.getViewName())) {
                String out = viewResolver.viewResolver(mv);
                if (out != null) {
                    resp.getWriter().write(out);
                    break;
                }
            }
        }
    }

    private XMHandlerMapping getHandler(HttpServletRequest req) {

        if (this.handlerMappings.isEmpty()) {
            return null;
        }

        String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        url = url.replace(contextPath, "").replaceAll("/+", "/");

        for (XMHandlerMapping handlerMapping : handlerMappings) {
            Matcher matcher = handlerMapping.getUrl().matcher(url);
            if (!matcher.matches()) {
                continue;
            }

            return handlerMapping;
        }

        return null;
    }

    private XMHandlerAdapter getHandlerAdapter(XMHandlerMapping handler) {

        if (this.handlerAdapters.isEmpty()) {
            return null;
        }

        return this.handlerAdapters.get(handler);
    }

    private void initRequestToViewNameTranslator(XMApplicationContext context) {
    }

    private void initHandlerExceptionResolvers(XMApplicationContext context) {
    }

    private void initThemeResolver(XMApplicationContext context) {
    }

    private void initLocaleResolver(XMApplicationContext context) {
    }

    private void initMultipartResolver(XMApplicationContext context) {
    }

    private void initHandlerMapping() {
    }

    private void doAutowrite() {
        if (beanMap.isEmpty()) {
            return;
        }

        for (Map.Entry<String, Object> entry : beanMap.entrySet()) {
            Field[] fields = entry.getValue().getClass().getDeclaredFields();

            for (Field field : fields) {
                if (!field.isAnnotationPresent(Autowired.class)) {
                    continue;
                }

                Autowired autowired = field.getAnnotation(Autowired.class);

                String beanName = autowired.value().trim();

                if ("".equals(beanName)) {
                    beanName = field.getType().getName();
                }
                field.setAccessible(true);

                try {
                    field.set(entry.getValue(), beanMap.get(beanName));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private void doRegistry() {
        if (classNames.isEmpty()) {
            return;
        }

        for (String className : classNames) {
            try {
                Class<?> clazz = Class.forName(className);

                //在Spring中用的多个子方法（策略模式）来处理
                if (clazz.isAnnotationPresent(Controller.class)) {

                    String beanName = lowerFirstCase(clazz.getSimpleName());

                    //在Spring这个阶段不会直接put instance，这里put的是BeanDefinition
                    beanMap.put(beanName, clazz.newInstance());
                } else if (clazz.isAnnotationPresent(Service.class)) {
                    Service service = clazz.getAnnotation(Service.class);

                    //默认用类名首字母注入
                    //如果自己定义了beanName，那么优先使用自己定义的beanName
                    //如果是一个接口，使用接口的类型去自动注入

                    //在Spring中同样会分别调用不同的方法 autowriedByName  autowritedByType

                    String beanName = service.value();

                    if ("".equals(beanName.trim())) {
                        beanName = lowerFirstCase(clazz.getSimpleName());
                    }

                    Object instance = clazz.newInstance();

                    beanMap.put(beanName, instance);

                    Class<?>[] interfaces = clazz.getInterfaces();

                    for (Class<?> c : interfaces) {
                        beanMap.put(c.getName(), instance);
                    }

                } else {
                    continue;
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
        }

    }

    private void doScanner(String pakName) {

        URL url = this.getClass().getClassLoader().getResource("/" + pakName.replaceAll("\\.", "/"));

        File classDir = new File(url.getFile());

        for (File file : classDir.listFiles()) {
            if (file.isDirectory()) {
                doScanner(pakName + "." + file.getName());
            } else {
                classNames.add(pakName + "." + file.getName().replace(".class", ""));

            }
        }

    }

    private void doLoadConfig(String location) {
        //在Spring中是通过Reader去查找和定位
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(location.replace("classpath:", ""));


        try {
            contextConfig.load(is);
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

    private String lowerFirstCase(String str) {
        if (!str.isEmpty()) {
            char[] chars = str.toCharArray();
            chars[0] += 32;
            return String.valueOf(chars);
        }
        return "";
    }


}
