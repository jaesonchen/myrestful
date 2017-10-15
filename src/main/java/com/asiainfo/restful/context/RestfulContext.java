package com.asiainfo.restful.context;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * 
 * @Description: RestContext holds HttpServletRequest and HttpServletResponse object in thread local.
 * 
 * @author       zq
 * @date         2017年10月13日  上午11:19:41
 * Copyright: 	  北京亚信智慧数据科技有限公司
 */
public class RestfulContext {

    private static ThreadLocal<RestfulContext> threadLocal = new ThreadLocal<RestfulContext>();
    private final HttpServletRequest req;
    private final HttpServletResponse res;

    private RestfulContext(HttpServletRequest req, HttpServletResponse res) {
        this.req = req;
        this.res = res;
    }

    public static RestfulContext getRestContext() {
        return threadLocal.get();
    }

    public static void initRestContext(HttpServletRequest req, HttpServletResponse res) {
        threadLocal.set(new RestfulContext(req, res));
    }

    public static void destroyRestContext() {
        threadLocal.set(null);
    }

    public HttpServletRequest getHttpServletRequest() {
        return req;
    }

    public HttpServletResponse getHttpServletResponse() {
        return res;
    }

    public HttpSession getHttpSession() {
        return req.getSession();
    }
}
