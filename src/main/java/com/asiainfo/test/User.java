package com.asiainfo.test;

import java.io.Serializable;
import java.util.List;

/**
 * @Description: TODO
 * 
 * @author       zq
 * @date         2017年10月13日  下午4:09:46
 * Copyright: 	  北京亚信智慧数据科技有限公司
 */
public class User implements Serializable {

	/** serialVersionUID */
	private static final long serialVersionUID = 1L;
	
	private long userId;
	private String name;
	private List<Order> list;
	public long getUserId() {
		return userId;
	}
	public void setUserId(long userId) {
		this.userId = userId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<Order> getList() {
		return list;
	}
	public void setList(List<Order> list) {
		this.list = list;
	}
	@Override
	public String toString() {
		return "User [userId=" + userId + ", name=" + name + ", list=" + list + "]";
	}
}
