<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<!--  No scriptlets!!! 
	  See http://download.oracle.com/javaee/5/tutorial/doc/bnakc.html 
-->
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:useBean id="model" class="client.presentation.web.model.QueryTheatresModel" scope="request"/>
<html>
<style type="text/css">  
#maintable {width: 800px; margin: 0 auto;}  
  
 #maintable td.red {color: #ff9933;}  
 #maintable td.blue {color:#00F;} 
 #maintable td.green {color:"green";} 
  
</style>  

<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<link rel="stylesheet" type="text/css" href="/resources/app.css"> 
<title>Choose a Seat</title>
</head>
<body>



<h2>Choose a Seat</h2>
<form action="/seatReply" method="post">
   <c:if test="${empty model.seats}">
	<p>Seats</p>
	<table id= "maintables">
		<tr>
	    	<th>Occupied Seats</th>
	    </tr>
	 <c:forEach var="row" items="${model.seats}">
		  <c:forEach var="seat" items="${row}">
		  	<tr>
		    	<c:when test="${row == 'FREE'}">
		    		<td class="green">	    		</td>
		    	</c:when>
		    	<c:when test="${row == 'OCCUPIED'}">
		    		<td class="blue">	    		</td>
		    	</c:when>
		    	<c:when test="${row == 'RESERVED'}">
		    		<td class="red">	    		</td>
		    	</c:when>
		    	
		    	
		  	</tr>
	  	  </c:forEach>
	  </c:forEach>
	</table>
   </c:if>
   <div class="mandatory_field">
   		<label for="reservedSeat">Your reserved seat: "$model.seat"</label> 
   		
    </div>
   <div class="mandatory_field">
   		<label for="theatreId">Please enter one of: YES for accept this seat; CAN for cancelling; XYY
for choosing another seat:</label> 
   		<input type="text" name="result" value=""/>
    </div>
    
   <input type="hidden" name="clientId" value="${model.clientId}"/>
   <input type="hidden" name="seat" value="${model.seat}"/>
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