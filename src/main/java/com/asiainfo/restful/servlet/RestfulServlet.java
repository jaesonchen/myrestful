package com.asiainfo.restful.servlet;

import java.io.IOException;
import java.util.stream.Stream;

import javax.servlet.GenericServlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.asiainfo.restful.handler.RestfulHandler;

/**
 * 
 * @Description: A REST API filter to handle REST API calls.
 * 
 * @author       zq
 * @date         2017年10月13日  下午1:57:06
 * Copyright: 	  北京亚信智慧数据科技有限公司
 */
public class RestfulServlet extends GenericServlet {

	final Log log = LogFactory.getLog(getClass());
	
	/** serialVersionUID */
	private static final long serialVersionUID = 1L;
    String urlPrefix = "";
    RestfulHandler handler = null;

    /**
     * Set RestApiHandler to handle REST API.
     * 
     * @param handler Instance of RestApiHandler.
     */
    public void setRestfulHandler(RestfulHandler handler) {
        this.handler = handler;
    }

    /**
     * Set URL prefix. e.g. "/api/v1".
     * 
     * @param prefix Prefix string, or null if no prefix.
     */
    public void setUrlPrefix(String prefix) {
    	
        prefix = (prefix == null) ? "" : prefix.trim();
        if (prefix.endsWith("/")) {
            prefix = prefix.substring(0, prefix.length() - 1);
        }
        if (!prefix.isEmpty() && !prefix.startsWith("/")) {
            log.error("Invalid urlPrefix: must start with /, but actual is: " + prefix);
            throw new IllegalArgumentException("Invalid urlPrefix parameter: " + prefix);
        }
        log.info("Set urlPrefix of RestApiFilter to: " + prefix);
        this.urlPrefix = prefix;
    }

    @Override
    public void init() throws ServletException {
    	
        log.info("Init RestfuleServlet...");
        if (this.handler == null) {
            this.handler = new RestfulHandler();
        }
        setUrlPrefix(getInitParameter("urlPrefix"));
        String handlers = getInitParameter("handlers");
        if (handlers != null) {
            Stream.of(handlers.split("\\,")).map((s) -> {
                return s.trim();
            }).filter((s) -> {
                return !s.isEmpty();
            }).forEach((s) -> {
                this.handler.registerHandler(s);
            });
        }
    }

    @Override
    public void service(ServletRequest request, ServletResponse response) throws ServletException, IOException {
    	
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        String path = req.getRequestURI().substring(req.getContextPath().length());
        //log.info("path=" + path);
        if (path.startsWith(this.urlPrefix)) {
            String apiUrl = path.substring(this.urlPrefix.length());
            if (apiUrl.startsWith("/")) {
                log.info("Process request: method=" + req.getMethod() + ", uri=" + apiUrl);
                this.handler.process(req, res, req.getMethod(), apiUrl);
                return;
            }
        }
        res.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Method Not Allowed");
    }

    @Override
    public void destroy() {
        log.info("Destroy RestfuleServlet...");
        this.handler = null;
    }

}
