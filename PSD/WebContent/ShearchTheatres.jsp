<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<jsp:useBean id="model" class="client.presentation.web.model.QueryTheatresModel" scope="request"/>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Widebox</title>
</head>
<body>
<form method="post" action="QueryTheatre">
	<div class="mandatory_field">
		<label for="movie">Movies name for search:</label> 
		<input type="text" name="movie" value=""/> <br/>
    </div>
   <div class="button" align="right">
   		<input type="submit" value="Send">
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