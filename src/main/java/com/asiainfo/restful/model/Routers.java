package com.asiainfo.restful.model;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.asiainfo.restful.annotation.DELETE;
import com.asiainfo.restful.annotation.GET;
import com.asiainfo.restful.annotation.POST;
import com.asiainfo.restful.annotation.PUT;
import com.asiainfo.restful.annotation.Path;
import com.asiainfo.restful.callback.Callable;
import com.asiainfo.restful.exception.PathNotFoundException;
import com.asiainfo.restful.util.ReflectUtils;

/**
 * 
 * @Description: A collection of Route objects.
 *               解析controller的@Path方法，按静态路径、正则路径进行注册，在request时负责查找对应的Callable，并进行回调
 * 
 * @author       zq
 * @date         2017年10月13日  上午10:44:43
 * Copyright: 	  北京亚信智慧数据科技有限公司
 */
public class Routers {

    Log log = LogFactory.getLog(getClass());

    final Map<String, Map<String, Callable>> staticMethods = new HashMap<String, Map<String, Callable>>();
    final Map<String, Callable> staticGetCallables = new HashMap<String, Callable>();
    final Map<String, Callable> staticPostCallables = new HashMap<String, Callable>();
    final Map<String, Callable> staticPutCallables = new HashMap<String, Callable>();
    final Map<String, Callable> staticDeleteCallables = new HashMap<String, Callable>();

    final Map<String, List<Callable>> regexMethods = new HashMap<String, List<Callable>>();
    final List<Callable> regexGetCallables = new ArrayList<Callable>();
    final List<Callable> regexPostCallables = new ArrayList<Callable>();
    final List<Callable> regexPutCallables = new ArrayList<Callable>();
    final List<Callable> regexDeleteCallables = new ArrayList<Callable>();

    final static List<Class<? extends Annotation>> HTTP_ANNOS = Arrays.asList(
            GET.class,
            POST.class,
            PUT.class,
            DELETE.class);

    public Routers() {
    	
        staticMethods.put("GET", staticGetCallables);
        staticMethods.put("POST", staticPostCallables);
        staticMethods.put("PUT", staticPutCallables);
        staticMethods.put("DELETE", staticDeleteCallables);
        
        regexMethods.put("GET", regexGetCallables);
        regexMethods.put("POST", regexPostCallables);
        regexMethods.put("PUT", regexPutCallables);
        regexMethods.put("DELETE", regexDeleteCallables);
    }

    /**
     * 
     * @Description: 查找处理类的所有方法，并注册
     * 
     * @param handler
     */
    public void addHandler(Object handler) {
    	
        Class<?> clazz = handler.getClass();
        for (Method m : ReflectUtils.getAllMethods(clazz)) {
            if (m.isAnnotationPresent(Path.class)) {
                Annotation httpMethod = null;
                for (Class<? extends Annotation> anno : HTTP_ANNOS) {
                    if (m.isAnnotationPresent(anno)) {
                        if (httpMethod == null) {
                            httpMethod = m.getAnnotation(anno);
                        }
                        else {
                            throw new IllegalArgumentException("Found more than one http method definition: @" 
                            		+ anno.getSimpleName() + ", @"+ httpMethod.getClass().getSimpleName() 
                            		+ " at method: " + clazz.getName() + "." + m.getName() + "()");
                        }
                    }
                }
                if (httpMethod == null) {
                    throw new IllegalArgumentException("Not found http method annotation at method: " 
                    		+ clazz.getName() + "." + m.getName() + "()");
                }
                if (Modifier.isStatic(m.getModifiers())) {
                    throw new IllegalArgumentException("Invalid static method: " + clazz.getName() + "." + m.getName() + "()");
                }
                if (Modifier.isAbstract(m.getModifiers())) {
                    throw new IllegalArgumentException("Invalid abstract method: " + clazz.getName() + "." + m.getName() + "()");
                }
                addHandler(handler, clazz, m, httpMethod.annotationType().getSimpleName(), m.getAnnotation(Path.class).value());
            }
            else {
                for (Class<? extends Annotation> anno : HTTP_ANNOS) {
                    if (m.isAnnotationPresent(anno)) {
                        throw new IllegalArgumentException("Annotation found but @Path is missing at method: " + clazz.getName() + "." + m.getName() + "()");
                    }
                }
            }
        }
    }

    /**
     * 
     * @Description: 注册处理方法
     * 
     * @param handler
     * @param clazz
     * @param method
     * @param httpMethod
     * @param path
     */
    protected void addHandler(Object handler, Class<?> clazz, Method method, String httpMethod, String path) {
    	
        Callable callable = new Callable(handler, clazz, method, httpMethod, path);
        if (callable.isStaticPath()) {
            Map<String, Callable> map = staticMethods.get(httpMethod);
            if (map.containsKey(path)) {
                throw new IllegalArgumentException("Duplicate handler for " + httpMethod + " " + path);
            }
            map.put(path, callable);
            log.info(httpMethod + ": " + path + ", handler: " + toHandlerString(clazz, method));
        }
        else {
            List<Callable> callables = this.regexMethods.get(httpMethod);
            // check if duplicate:
            for (Callable c : callables) {
                if (c.getPath().equals(path)) {
                    throw new IllegalArgumentException("Duplicate handler for " + httpMethod + " " + path);
                }
            }
            callables.add(callable);
            log.info(httpMethod + ": " + path + ", handler: " + toHandlerString(clazz, method));
        }
    }

    public String toHandlerString(Class<?> clazz, Method method) {
        List<String> paramNames = new ArrayList<String>();
        for (Parameter param : method.getParameters()) {
            paramNames.add(param.getName());
        }
        return clazz.getName() + "." + method.getName() + "(" + String.join(", ", paramNames) + ")";
    }

    /**
     * 
     * @Description: 调用路径处理器
     * 
     * @param httpMethod
     * @param path
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public Object call(String httpMethod, String path, HttpServletRequest request, HttpServletResponse response) throws Exception {
        
    	// try find static handler
        Callable sc = staticMethods.get(httpMethod).get(path);
        if (sc != null) {
            return sc.call(null, request, response);
        }
        // try find regex handler
        List<Callable> list = regexMethods.get(httpMethod);
        for (Callable c : list) {
            Map<String, String> pathValues = c.getRoute().matches(path);
            if (pathValues != null) {
                return c.call(pathValues, request, response);
            }
        }
        throw new PathNotFoundException();
    }
}