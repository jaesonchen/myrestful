<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%
	String contextPath = request.getContextPath().toString();
%>
<!DOCTYPE html>
<html>
<head>
<script src="<%=contextPath%>/js/jquery-1.11.3.js"></script>
</head>
<body>
<div id="feedback"></div>
<button type=button id="bth-list">list</button>
<button type=button id="bth-get">get</button>
<button type=button id="bth-post">post</button>
<button type=button id="bth-put">put</button>
<button type=button id="bth-delete">delete</button>
<script type="text/javascript">

	$("#bth-list").click(function() {
	    $.ajax({
	        type: "GET",
	        url: "<%=contextPath%>/restful/users",
	        contentType: "application/json; charset=utf-8",
	        data: {},
	        dataType: "json",
	        success: function (data) {
	        	display(data);
	        },
	        error: function (data) {
	        	alert("提交失败！");
	        }
	    });
	});

	$("#bth-get").click(function() {
	    $.ajax({
	        type: "GET",
	        url: "<%=contextPath%>/restful/users/1001",
	        contentType: "application/json; charset=utf-8",
	        data: {},
	        dataType: "json",
	        success: function (data) {
	        	display(data);
	        },
	        error: function (data) {
	        	alert("提交失败！");
	        }
	    });
	});

	$("#bth-post").click(function() {
	    $.ajax({
	        type: "POST",
	        url: "<%=contextPath%>/restful/users/1002",
	        contentType: "application/json; charset=utf-8",
	        data: JSON.stringify(GetPostData()),
	        dataType: "json",
	        success: function (data) {
	        	display(data);
	        },
	        error: function (data) {
	        	alert("提交失败！");
	        }
	    });
	});
	function GetPostData() {
	    return {
	    	"userId":1001,
	    	"name":"chenzq",
	    	"list":[{"id":"0001","total":12345.6},
	    	        {"id":"0002","total":65432.1}]
	    };
	}
	
	$("#bth-put").click(function() {
	    $.ajax({
	        type: "PUT",
	        url: "<%=contextPath%>/restful/users",
	        contentType: "application/json; charset=utf-8",
	        data: JSON.stringify(GetPutData()),
	        dataType: "json",
	        success: function (data) {
	        	display(data);
	        },
	        error: function (data) {
	        	alert("提交失败！");
	        }
	    });
	});
	function GetPutData() {
	    return {
	    	"name":"jaesonchen",
	    	"list":[{"id":"0003","total":12345.6},
	    	        {"id":"0004","total":65432.1}]
	    };
	}	

	$("#bth-delete").click(function() {
	    $.ajax({
	        type: "DELETE",
	        url: "<%=contextPath%>/restful/users/1003",
	        contentType: "application/json; charset=utf-8",
	        data: {},
	        dataType: "json",
	        success: function (data) {
	        	display(data);
	        },
	        error: function (data) {
	        	alert("提交失败！");
	        }
	    });
	});
	
	function display(data) {
		var json = "<h4>Ajax Response</h4><pre>"
				+ JSON.stringify(data, null, 4) + "</pre>";
		$('#feedback').html(json);
	}
</script>
</body>
</html>
