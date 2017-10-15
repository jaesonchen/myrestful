package com.asiainfo.restful.model;

import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.asiainfo.restful.annotation.PathVariable;

/**
 * 
 * @Description: Route by regular express.
 * 
 * @author       zq
 * @date         2017年10月13日  上午11:34:09
 * Copyright: 	  北京亚信智慧数据科技有限公司
 */
public class Route {

    static final Log log = LogFactory.getLog(Route.class);
    static final Pattern RE_ROUTE_VAR = Pattern.compile("\\:([A-Za-z\\_][A-Za-z0-9]*)");

    final String[] parameters;
    final Pattern regexPath;

    public Route(String path) {
        PatternAndNames pan = compile(path);
        this.regexPath = pan.pattern;
        this.parameters = pan.names;
    }

    public String[] getParameters() {
		return parameters;
	}
	public Pattern getRegexPath() {
		return regexPath;
	}

	/**
     * 
     * @Description: 拆分路径的正则表达式和路径变量
     * 
     * @param path
     * @return
     */
    protected PatternAndNames compile(String path) {
    	
        StringBuilder sb = new StringBuilder();
        sb.append("^");
        int start = 0;
        List<String> names = new ArrayList<String>();
        for (;;) {
            Matcher matcher = RE_ROUTE_VAR.matcher(path);
            boolean found = matcher.find(start);
            if (found) {
                if (start == matcher.start()) {
                    log.warn("URL pattern has possible error: \"" + path + "\", at " + start);
                }
                else {
                    appendStatic(sb, path.substring(start, matcher.start()));
                }
                String name = matcher.group(1);
                start = matcher.end();
                appendVar(sb, name);
                names.add(name);
            }
            else {
                appendStatic(sb, path.substring(start));
                break;
            }
        }
        if (names.isEmpty()) {
            throw new IllegalArgumentException("Cannot compile path to a valid regular expression: " + path);
        }
        sb.append("$");
        return new PatternAndNames(Pattern.compile(sb.toString()), names.toArray(new String[names.size()]));
    }

    /**
     * 
     * @Description: 正则表达式路径变量
     * 
     * @param sb
     * @param name
     */
    protected static void appendVar(StringBuilder sb, String name) {
        sb.append("(?<").append(name).append(">[^\\/]+)");
    }

    /**
     * 
     * @Description: 正则表达式路径
     * 
     * @param sb
     * @param s
     */
    protected static void appendStatic(StringBuilder sb, String s) {
    	
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c >= 'A' && c <= 'Z') {
                sb.append(c);
            }
            else if (c >= 'a' && c <= 'z') {
                sb.append(c);
            }
            else if (c >= '0' && c <= '9') {
                sb.append(c);
            }
            else {
                sb.append('\\').append(c);
            }
        }
    }

    /**
     * 
     * @Description: 是否存在路径变量
     * 
     * @param p
     * @return
     */
    public boolean hasParameter(Parameter p) {
    	
    	PathVariable pv = p.getAnnotation(PathVariable.class);
    	if (null != pv) {
	        for (String s : this.parameters) {
	            if (s.equals(pv.value())) {
	                return true;
	            }
	        }
    	}
        return false;
    }
    
    /**
     * 
     * @Description: 返回路径变量名称
     * 
     * @param p
     * @return
     */
    public String getPathVariable(Parameter p) {
    	
    	PathVariable pv = p.getAnnotation(PathVariable.class);
    	if (null != pv) {
    		return pv.value();
    	}
    	return null;
    }
    
    /**
     * 
     * @Description: 请求路径的路径变量映射
     * 
     * @param path
     * @return
     */
    public Map<String, String> matches(String path) {
    	
        Matcher matcher = regexPath.matcher(path);
        if (matcher.matches()) {
            Map<String, String> map = new HashMap<String, String>();
            for (String param : this.parameters) {
                map.put(param, matcher.group(param));
                //System.out.println(param + "=" + map.get(param));
            }
            return map;
        }
        return null;
    }
}

class PatternAndNames {

    final Pattern pattern;
    final String[] names;

    PatternAndNames(Pattern pattern, String[] names) {
        this.pattern = pattern;
        this.names = names;
    }
}
