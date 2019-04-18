package com.asiainfo.restful.handler;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.asiainfo.restful.context.RestfulContext;
import com.asiainfo.restful.exception.PathNotFoundException;
import com.asiainfo.restful.model.Routers;
import com.asiainfo.restful.util.ClassFinder;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * @Description: RestfulHandler serves JSON-REST API request, and send JSON-Response.
 * 
 * @author       zq
 * @date         2017年10月13日  上午9:39:07
 * Copyright: 	  北京亚信智慧数据科技有限公司
 */
public class RestfulHandler {

    Log log = LogFactory.getLog(getClass());
    static final String GET = "GET";
    static final String POST = "POST";
    static final String PUT = "PUT";
    static final String DELETE = "DELETE";
    static final String CONTENTTYPE_JSON = "application/json";
    
    Routers routes = new Routers();

    public void registerHandler(String name) {
        for (Class<?> clazz : new ClassFinder().findClasses(name)) {
            addHandler(clazz);
        }
    }

    public void addHandler(Class<?> clazz) {
        try {
            routes.addHandler(clazz.newInstance());
        }
        catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 
     * @Description: 处理请求
     * 
     * @param req
     * @param res
     * @param method
     * @param path
     * @throws IOException
     */
    public void process(HttpServletRequest req, HttpServletResponse res, String method, String path) throws IOException {
    	
        switch (method) {
	        case "GET":
	            processRequest(req, res, "GET", path);
	            break;
	        case "POST":
	            processRequest(req, res, "POST", path);
	            break;
	        case "PUT":
	            processRequest(req, res, "PUT", path);
	            break;
	        case "DELETE":
	            processRequest(req, res, "DELETE", path);
	            break;
	        default:
	        	processUnsupportedRequest(req, res, method, path);
	            break;
        }
    }

    /**
     * 
     * @Description: 调用Routers进行request分发，并将结果json写入response
     * 
     * @param req
     * @param res
     * @param method
     * @param path
     * @throws IOException
     */
    protected void processRequest(HttpServletRequest req, HttpServletResponse res, String method, String path) throws IOException {
    	
        // check content type
        if (!GET.equals(method) && req.getContentLength() > 0 && !checkContentType(req.getContentType())) {
            log.debug("415 UNSUPPORTED MEDIA TYPE: not a json request.");
            res.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE, "Request must be application/json.");
            return;
        }
        RestfulContext.initRestContext(req, res);
        try {
            Object ret = this.routes.call(method, path, req, res);
            if (ret instanceof Void) {
                return;
            }
            res.setCharacterEncoding("UTF-8");
            res.setContentType("application/json");
            Writer writer = res.getWriter();
    		writer.write(new ObjectMapper().writeValueAsString(ret));
            writer.flush();
        }
        catch (PathNotFoundException e) {
        	res.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
        catch (Exception e) {
            log.error("Process failed.", e);
            res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        finally {
            RestfulContext.destroyRestContext();
        }
    }
    
    /**
     * 
     * @Description: 处理不支持Method的请求
     * 
     * @param req
     * @param res
     * @param method
     * @param path
     * @throws IOException
     */
    protected void processUnsupportedRequest(HttpServletRequest req, HttpServletResponse res, String method, String path) throws IOException {
        res.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Method Not Allowed");
    }
    
    /**
     * 
     * @Description: 判断是否application/json 
     * 
     * @param contentType
     * @return
     */
    protected boolean checkContentType(String contentType) {
    	
        if (StringUtils.isEmpty(contentType)) {
            return false;
        }
        String type = contentType.toLowerCase();
        if (CONTENTTYPE_JSON.equals(type)) {
            return true;
        }
        if (type.startsWith(CONTENTTYPE_JSON)) {
            char ch = type.charAt(16);
            return ch == ' ' || ch == ';';
        }
        return false;
    }
}
