package com.asiainfo.restful.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @Description: Utils for internal use only.
 * 
 * @author       zq
 * @date         2017年10月13日  下午1:06:34
 * Copyright: 	  北京亚信智慧数据科技有限公司
 */
public class ReflectUtils {

	/**
	 * 
	 * @Description: 返回指定类的所有方法（包括父类）
	 * 
	 * @param clazz
	 * @return
	 */
	public static List<Method> getAllMethods(Class<?> clazz) {
		
        List<Method> methods = new ArrayList<Method>();
        while (!clazz.equals(Object.class)) {
            for (Method m : clazz.getDeclaredMethods()) {
                methods.add(m);
            }
            clazz = clazz.getSuperclass();
        }
        return methods;
    }
}
