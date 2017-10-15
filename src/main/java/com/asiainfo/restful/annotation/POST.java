package com.asiainfo.restful.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * @Description: Mark a POST request of REST API.
 * 
 * @author       zq
 * @date         2017年10月13日  上午9:27:35
 * Copyright: 	  北京亚信智慧数据科技有限公司
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface POST {

}
