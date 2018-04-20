package com.belizer.spring.servlet;

import com.belizer.spring.annotation.Autowired;
import com.belizer.spring.annotation.Controller;
import com.belizer.spring.annotation.RequestMapping;
import com.belizer.spring.annotation.Service;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;


public class DispatcherServlet extends HttpServlet {

    private Properties contextConfig=new Properties();
    private List<String> beanNameList=new ArrayList<>();
    private Map<String,Object> beanMap=new HashMap<>();
    private Map<String,Method> handlerMapping=new HashMap<>();
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String requestURI=req.getRequestURI();
        System.out.println(requestURI);

        for(Map.Entry entry:handlerMapping.entrySet()){
            if(requestURI.equals(entry.getKey())){
                Method method=(Method)entry.getValue();
                Class clazz=method.getDeclaringClass();
                //在容器中查找method的执行对象，执行method
                for(Map.Entry bean_entry:beanMap.entrySet()){
                    Object obj=bean_entry.getValue();
                    if(obj.getClass()==clazz){
                        try {
                            method.invoke(bean_entry.getValue(),method.getParameters());
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }

                }

            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
//        super.init(config);
//        for(;true;){}
        //初始化
        //1，定位资源文件
        String contextConfigLocation=config.getInitParameter("contextConfigLocation");
        try {
            doLoadConfig(contextConfigLocation);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //2，加载资源文件
        doScanner(contextConfig.getProperty("basePackage"));
        //3，注册bean
        doRegister();
        //4，依赖注入-只考虑依赖注入的注解只在成员变量上
        doAutowired();
        //请求路径映射
        doInitialHandler();
        //初始化完成

    }

    private void doInitialHandler() {
        if(beanMap.isEmpty()){return;}

        for(Map.Entry entry:beanMap.entrySet()){
            if(entry.getValue().getClass().getAnnotation(Controller.class)!=null){
                Class clazz=entry.getValue().getClass();
                RequestMapping c_rm=(RequestMapping)clazz.getAnnotation(RequestMapping.class);//这是类的映射路径
                for(Method method:clazz.getMethods()){
                    if(method.getDeclaredAnnotation(RequestMapping.class)!=null){
                        RequestMapping m_rm=(RequestMapping)method.getDeclaredAnnotation(RequestMapping.class);
                        handlerMapping.put("/"+c_rm.value()+m_rm.value(),method);
                    }
                }
            }
        }
    }

    private void doAutowired() {
        if(beanMap.isEmpty()){return;}

        for(Map.Entry entry:beanMap.entrySet()){
            Field[] fields=entry.getValue().getClass().getDeclaredFields();
            for(Field field:fields){
                if(!field.isAnnotationPresent(Autowired.class)){continue;}
                Class clazz=field.getType();

                //不是太清楚spring是怎么按类型注入的，目前按名字注入
                String name=clazz.getSimpleName();
                field.setAccessible(true);
                try {
                    field.set(entry.getValue(),beanMap.get(lowerCaseFirst(name)));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void doRegister() {
        if(beanNameList.size()==0){return;}

        for(String beanName:beanNameList){
            try {
                Class clazz=Class.forName(beanName);
                if(clazz.isAnnotationPresent(Controller.class)){
                    beanMap.put(beanName,clazz.newInstance());
                }else if(clazz.isAnnotationPresent(Service.class)){
                    Service service=(Service)clazz.getAnnotation(Service.class);
                    if(!service.value().isEmpty()){
                        //如果自己定义了beanName，那么优先使用自己定义的beanName
                        beanMap.put(service.value(),clazz.newInstance());
                    }else {
                        //默认用类名首字母小写注册
                        beanName=beanName.substring(beanName.lastIndexOf(".")+1);
                        beanName=lowerCaseFirst(beanName);
                        beanMap.put(beanName,clazz.newInstance());
                    }
                    //如果是一个接口，使用接口的类型去自动注册
//                    Class[] interfaces=clazz.getInterfaces();
                }else{
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

    private void doScanner(String packageName) {
        String path=packageName.replaceAll("\\.","/");//注意第一个参数是正则表达式
        URL url=this.getClass().getClassLoader().getResource(path);
        File file=new File(url.getFile());
        for(File f:file.listFiles()){
            if(f.isDirectory()){
                doScanner(packageName+"."+f.getName());
            }else{
                beanNameList.add(packageName+"."+f.getName().replace(".class",""));
            }
        }
    }


    private void doLoadConfig(String location) throws IOException {
        InputStream is=this.getClass().getClassLoader().getResourceAsStream(location.replace("classpath:",""));
        contextConfig.load(is);
    }

    private String lowerCaseFirst(String oldStr){
        char[] chars=oldStr.toCharArray();
        chars[0]+=32;
        return String.valueOf(chars);
    }

}
