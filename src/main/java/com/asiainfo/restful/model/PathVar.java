package com.asiainfo.restful.model;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 
 * @Description: A Var holds information of a method argument.
 * 
 * @author       zq
 * @date         2017年10月13日  上午10:42:48
 * Copyright: 	  北京亚信智慧数据科技有限公司
 */
public class PathVar {

    public static final int PATH_VAR = 0;
    public static final int JSON_VAR = 1;
    public static final int QUERY_VAR = 2;
    public static final int REQUEST_VAR = 3;
    public static final int RESPONSE_VAR = 4;

    final int varType;
    final Class<?> argType;
    final String name;
    final int index;

    public PathVar(int varType, Class<?> argType, String name, int index) {
        this.varType = varType;
        this.argType = argType;
        this.name = name;
        this.index = index;
    }
 
	public int getVarType() {
		return varType;
	}
	public Class<?> getArgType() {
		return argType;
	}
	public String getName() {
		return name;
	}
	public int getIndex() {
		return index;
	}

	public static PathVar createPathVar(Class<?> argType, String name, int index) {
        return new PathVar(PATH_VAR, argType, name, index);
    }

    public static PathVar createJsonVar(Class<?> argType, String name, int index) {
        return new PathVar(JSON_VAR, argType, name, index);
    }

    public static PathVar createRequestVar(String name, int index) {
        return new PathVar(REQUEST_VAR, HttpServletRequest.class, name, index);
    }

    public static PathVar createQueryVar(String name, int index) {
        return new PathVar(QUERY_VAR, Map.class, name, index);
    }

    public static PathVar createResponseVar(String name, int index) {
        return new PathVar(RESPONSE_VAR, HttpServletResponse.class, name, index);
    }
}
