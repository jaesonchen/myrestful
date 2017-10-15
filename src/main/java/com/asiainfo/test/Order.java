package com.asiainfo.test;

import java.io.Serializable;

/**
 * @Description: TODO
 * 
 * @author       zq
 * @date         2017年10月13日  下午4:11:11
 * Copyright: 	  北京亚信智慧数据科技有限公司
 */
public class Order implements Serializable {

	/** serialVersionUID */
	private static final long serialVersionUID = 1L;
	private String id;
	private double total;
	
	public Order() {}
	public Order(String id, double total) {
		this.id = id;
		this.total = total;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public double getTotal() {
		return total;
	}
	public void setTotal(double total) {
		this.total = total;
	}
	@Override
	public String toString() {
		return "Order [id=" + id + ", total=" + total + "]";
	}
}
