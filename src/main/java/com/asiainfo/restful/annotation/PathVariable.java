package com.asiainfo.restful.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Description: TODO
 * 
 * @author       zq
 * @date         2017年10月13日  下午4:54:50
 * Copyright: 	  北京亚信智慧数据科技有限公司
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface PathVariable {
	String value();
}
