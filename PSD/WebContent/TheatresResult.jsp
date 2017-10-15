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
<title>Choose a Theatre</title>
</head>
<body>
<h2>Associar �rbitro a encontro</h2>
<form action="chooseSeat" method="post">
   <c:if test="${model.hasTheatres}">
	<p>Theatres</p>
	<table>
	<tr>
	    <th>Theatre Id</th>
	  </tr>
	 <c:forEach var="theatre" items="${model.theatres}">
		  <tr>
		    <td>"${theatre.id}"</td>
		  </tr>
	  </c:forEach>
	</table>
   </c:if>
   <div class="mandatory_field">
   		<label for="theatreId">Theatre Id:</label> 
   		<input type="text" name="theatreId" value=""/>
    </div>
   <input type="hidden" name="clientId" value="${model.clientId}"/>
   <div class="button" align="right">
   		<input type="submit" value="Choose Theatre">
   </div>
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