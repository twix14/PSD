<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<!--  No scriptlets!!! 
	  See http://download.oracle.com/javaee/5/tutorial/doc/bnakc.html 
-->
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:useBean id="model" class="client.presentation.web.model.QueryTheatresModel" scope="request"/>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<link rel="stylesheet" type="text/css" href="/resources/app.css"> 
<script language="javascript"> 

function proc(theatre){ 
  <input type="hidden" name="theatreId" value=theatre/>
} 

</script> 
<title>Choose a Theatre</title>
</head>
<body>
<h2>Theatres Result</h2>
<form id="myForm" action="/chooseSeat" method="post">
   <c:if test="${model.hasTheatres}">
	<p>Theatres</p>
	<ul style="list-style-type:disc">
	<c:forEach var="theatre" items="${model.theatres}">
		<!-- <li onclick='proc("${theatre}");myForm.submit();'>${theatre}</li> -->
		
		<li >${theatre}</li>
		
	  </c:forEach>
	</ul>
   </c:if>  
   <div class="mandatory_field">
   		<label for="theatreId">Theatre Id:</label> 
   		<input type="text" name="theatreId" value=""/>
    </div> 
   
   <div class="button" align="right">
   		<input type="submit" value="Choose Theatre">
   </div>
   
   
   <input type="hidden" name="clientId" value="${model.clientId}"/>
   <c:if test="${model.hasMessages}">
	<p>Mensagens</p>
	<ul>
	<c:forEach var="mensagem" items="${model.messages}">
		<li>${mensagem} 
	</c:forEach>
	</ul>
   </c:if>
</form>
</body>
</html>