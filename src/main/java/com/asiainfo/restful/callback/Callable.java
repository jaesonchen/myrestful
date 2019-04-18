package com.asiainfo.restful.callback;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import com.asiainfo.restful.model.PathVar;
import com.asiainfo.restful.model.Route;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * @Description: Callable object to hold method and argument informations.
 *               真正的请求处理回调方法，每个@Path对应一个method，在构建时解析method需要的参数列表，在回调时使用该参数列表从request中创建调用参数。
 * 
 * @author       zq
 * @date         2017年10月13日  上午9:42:49
 * Copyright: 	  北京亚信智慧数据科技有限公司
 */
public class Callable {

    final String path;
    final boolean staticPath;
    final Route route;
    final Object handler;
    final Method method;
    final PathVar[] vars;

    public Callable(Object handler, Class<?> clazz, Method method, String httpMethod, String path) {
    	
    	method.setAccessible(true);
        this.handler = handler;
        this.method = method;
        this.path = path;
        this.staticPath = path.indexOf(":") == (-1);
        this.route = staticPath ? null : new Route(path);
        this.vars = createVars(clazz, method, this.route, httpMethod);
    }

    public String getPath() {
		return path;
	}
	public boolean isStaticPath() {
		return staticPath;
	}
	public Route getRoute() {
		return route;
	}
	public Object getHandler() {
		return handler;
	}
	public Method getMethod() {
		return method;
	}
	public PathVar[] getVars() {
		return vars;
	}

	/**
     * 
     * @Description: 解析controller中@Path定义的url与处理方法method的参数映射关系
     * 
     * @param clazz
     * @param method
     * @param route
     * @param httpMethod
     * @return
     */
    protected PathVar[] createVars(Class<?> clazz, Method method, Route route, String httpMethod) {
    	
        List<PathVar> vars = new ArrayList<PathVar>();
        Parameter[] ps = method.getParameters();
        boolean foundJson = false;
        for (int index = 0; index < ps.length; index++) {
            Parameter p = ps[index];
            String varName = p.getName();
            Class<?> varType = p.getType();
            if (HttpServletRequest.class.equals(varType)) {
                vars.add(PathVar.createRequestVar(varName, index));
            }
            else if (HttpServletResponse.class.equals(varType)) {
                vars.add(PathVar.createResponseVar(varName, index));
            }
            //Map<String, String> query
            else if (isMapStringString(p)) {
                vars.add(PathVar.createQueryVar(varName, index));
            }
            //accept pathvar
            else if (route != null && route.hasParameter(p)) {
                if (!isValidPathVariableType(varType)) {
                    throw new IllegalArgumentException("Unsupported path variable \"" + varType.getName() + " " + varName + "\" in " + toHandlerString(clazz, method));
                }
                vars.add(PathVar.createPathVar(varType, route.getPathVariable(p), index));
            }
            //accept json
            else if (!"GET".equals(httpMethod)) {
                if (foundJson) {
                    throw new IllegalArgumentException("Duplicate json variable \"" + varType.getName() + " " + varName + "\" in " + toHandlerString(clazz, method));
                }
                vars.add(PathVar.createJsonVar(varType, varName, index));
                foundJson = true;
            }
            else {
                throw new IllegalArgumentException("Unknown parameter \"" + varType.getName() + " " + varName + "\" in " + toHandlerString(clazz, method));
            }
        }
        return vars.toArray(new PathVar[vars.size()]);
    }

    /**
     * 
     * @Description: 判断参数类型是否是Map<String, String>
     * 
     * @param p
     * @return
     */
    protected boolean isMapStringString(Parameter p) {
    	
        if (!Map.class.equals(p.getType())) {
            return false;
        }
        Type type = p.getParameterizedType();
        if (!(type instanceof ParameterizedType)) {
            return false;
        }
        ParameterizedType pt = (ParameterizedType) type;
        Type[] types = pt.getActualTypeArguments();
        if (types.length != 2) {
            return false;
        }
        return String.class.equals(types[0]) && String.class.equals(types[1]);
    }

    protected String toHandlerString(Class<?> clazz, Method method) {
    	
        List<String> paramNames = new ArrayList<String>();
        for (Parameter param : method.getParameters()) {
            paramNames.add(param.getName());
        }
        return clazz.getName() + "." + method.getName() + "(" + String.join(", ", paramNames) + ")";
    }

    /**
     * 
     * @Description: 路径对应的controller处理
     * 
     * @param pathVars
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public Object call(Map<String, String> pathVars, HttpServletRequest request, HttpServletResponse response) throws Exception {
        
    	// prepare arguments
        Object[] args = new Object[this.vars.length];
        for (int i = 0; i < args.length; i++) {
            PathVar var = this.vars[i];
            switch (var.getVarType()) {
	            case PathVar.PATH_VAR:
	                args[i] = convertPathVariable(pathVars.get(var.getName()), var.getArgType());
	                break;
	            case PathVar.JSON_VAR:
	                args[i] = parseJson(var.getArgType(), request);
	                break;
	            case PathVar.QUERY_VAR:
	                args[i] = createQuery(request);
	                break;
	            case PathVar.REQUEST_VAR:
	                args[i] = request;
	                break;
	            case PathVar.RESPONSE_VAR:
	                args[i] = response;
	                break;
	            default:
	                throw new RuntimeException("Bad var type: " + var.getVarType());
            }
        }
        try {
            return this.method.invoke(this.handler, args);
        }
        catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * 
     * @Description: request中的json 到controller 参数对象映射
     * 
     * @param type
     * @param req
     * @return
     * @throws IOException
     */
    protected Object parseJson(Class<?> type, HttpServletRequest req) throws IOException {
    	
        String encoding = req.getCharacterEncoding();
        if (StringUtils.isEmpty(encoding)) {
            encoding = "UTF-8";
        }
        Reader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(req.getInputStream(), encoding));
            //return new JsonBuilder().createReader(reader).parse(type);
            return new ObjectMapper().readValue(reader, type);
        }
        finally {
            if (reader != null) {
                reader.close();
            }
        }    
    }

    /**
     * 
     * @Description: 将request中的参数放入map
     * 
     * @param request
     * @return
     */
    protected Map<String, String> createQuery(HttpServletRequest request) {
    	
        Map<String, String> map = new HashMap<String, String>();
        for (Enumeration<String> e = request.getParameterNames(); e.hasMoreElements(); ) {
            String key = e.nextElement();
            map.put(key, request.getParameter(key));
        }
        return map;
    }

    //参数值类型转换
    interface Converter {
        Object convert(String str);
    }

    static final Map<String, Converter> CONVERTERS = initConverters();

    static Map<String, Converter> initConverters() {
    	
        Map<String, Converter> converters = new HashMap<String, Converter>();
        // to byte:
        Converter byteConverter = (str) -> {
            return Byte.parseByte(str);
        };
        converters.put(byte.class.getName(), byteConverter);
        converters.put(Byte.class.getName(), byteConverter);
        
        // to short:
        Converter shortConverter = (str) -> {
            return Short.parseShort(str);
        };
        converters.put(short.class.getName(), shortConverter);
        converters.put(Short.class.getName(), shortConverter);
        
        // to int:
        Converter intConverter = (str) -> {
            return Integer.parseInt(str);
        };
        converters.put(int.class.getName(), intConverter);
        converters.put(Integer.class.getName(), intConverter);
        
        // to long:
        Converter longConverter = (str) -> {
            return Long.parseLong(str);
        };
        converters.put(long.class.getName(), longConverter);
        converters.put(Long.class.getName(), longConverter);
        
        // to float:
        Converter floatConverter = (str) -> {
            return Float.parseFloat(str);
        };
        converters.put(float.class.getName(), floatConverter);
        converters.put(Float.class.getName(), floatConverter);
        
        // to double:
        Converter doubleConverter = (str) -> {
            return Double.parseDouble(str);
        };
        converters.put(double.class.getName(), doubleConverter);
        converters.put(Double.class.getName(), doubleConverter);
        
        // to number:
        converters.put(Number.class.getName(), (str) -> {
            try {
                return Long.parseLong(str);
            }
            catch (NumberFormatException e) {
                return Double.parseDouble(str);
            }
        });
        
        // to string:
        converters.put(String.class.getName(), (str) -> {
            return str;
        });
        return converters;
    }

    public boolean isValidPathVariableType(Class<?> clazz) {
        return CONVERTERS.containsKey(clazz.getName());
    }

    public Object convertPathVariable(String str, Class<?> clazz) {
        return CONVERTERS.get(clazz.getName()).convert(str);
    }
}
