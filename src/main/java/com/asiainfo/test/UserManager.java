package com.asiainfo.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.asiainfo.restful.annotation.DELETE;
import com.asiainfo.restful.annotation.GET;
import com.asiainfo.restful.annotation.POST;
import com.asiainfo.restful.annotation.PUT;
import com.asiainfo.restful.annotation.PathVariable;
import com.asiainfo.restful.annotation.Path;

/**
 * @Description: TODO
 * 
 * @author       zq
 * @date         2017年10月13日  下午4:13:22
 * Copyright: 	  北京亚信智慧数据科技有限公司
 */
public class UserManager {

	@GET
	@Path("/users")
	protected List<User> getUsers(Map<String, String> map) {
		
		List<User> result = new ArrayList<>();
		
		User user = new User();
        user.setUserId(1001);
        user.setName("chenzq");
        List<Order> list = new ArrayList<>();
        list.add(new Order("0001", 12345.6));
        list.add(new Order("0002", 65432.1));
        user.setList(list);
        result.add(user);
		
        user = new User();
        user.setUserId(1002);
        user.setName("jaesonchen");
        list = new ArrayList<>();
        list.add(new Order("0003", 12345.67));
        list.add(new Order("0004", 65432.10));
        user.setList(list);
        result.add(user);
        
		return result;
	}
	   
	@GET
    @Path("/users/:id")
    public User getUser(@PathVariable("id") long id) {
		
        User user = new User();
        user.setUserId(id);
        user.setName("chenzq");
        List<Order> list = new ArrayList<>();
        list.add(new Order("0001", 12345.6));
        list.add(new Order("0002", 65432.1));
        user.setList(list);
        return user;
    }
	
    @PUT
    @Path("/users")
    User saveUser(User user, HttpServletRequest request) {
    	
    	System.out.println(user);
    	user.setUserId(1001);
    	return user;
	}
    
    @POST
    @Path("/users/:id")
    public User updateUser(@PathVariable("id") long id, User user) {
    	
    	System.out.println(user);
    	user.setUserId(id);
        return user;
    }

    @DELETE
    @Path("/users/:id")
    private User deleteUser(@PathVariable("id") long id) {
    	
    	User user = new User();
        user.setUserId(id);
        user.setName("chenzq");
        List<Order> list = new ArrayList<>();
        list.add(new Order("0001", 12345.6));
        list.add(new Order("0002", 65432.1));
        user.setList(list);
        return user;
    }
}
