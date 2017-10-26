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
<style type="text/css">  
#maintable {width: 800px; margin: 0 auto;}  
  
 #maintable td.red {color: #ff0000;}  
 #maintable td.blue {color:#0000ff;} 
 #maintable td.green {color:#00ff00;} 
  
</style>  


<link rel="stylesheet" type="text/css" href="/resources/app.css"> 
<title>Choose a Seat</title>
</head>
<body>


<h3>Theatre: ${model.theatre}</h3>
<h2>Choose a Seat</h2>
<form action="seatReply" method="post">
   <c:if test="${!empty model.seats}">
	<table id= "maintable">
		<!-- <tr>
	    	<th></th>
	    </tr> -->
	 <c:forEach var="row" items="${model.seats}">
	 	<tr>
		  <c:forEach var="seat" items="${row}">
		  <c:choose>
		  	
		    	<c:when test="${seat.reserved}">
		    	
		    		<td class="green">R</td>
		    	</c:when>
		    	<c:when test="${seat.occupied}">
		    		<td class="blue">O</td>
		    	</c:when>
		    	<c:otherwise>
		    		<td class="red">F</td>
		    	</c:otherwise>
		    	
		  </c:choose>
		  </c:forEach>
		  </tr>  	
		  	
	  	  
	  </c:forEach>
	</table>
   </c:if>
   <div class="mandatory_field">
   		<label for="reservedSeat">Your reserved seat: ${model.seat}</label> 
   		
    </div>
   <div class="mandatory_field">
   		<label for="theatreId">Please enter one of: YES for accept this seat; CAN for cancelling; XYY
for choosing another seat:</label> 
   		<input type="text" name="result" value=""/>
    </div>
    
   <input type="hidden" name="clientId" value="${model.clientId}"/>
   <input type="hidden" name="seat" value="${model.seat}"/>
   <input type="hidden" name="theatre" value="${model.theatre}"/>
   <div class="button" align="right">
   		<input type="submit" value="Choose seat">
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