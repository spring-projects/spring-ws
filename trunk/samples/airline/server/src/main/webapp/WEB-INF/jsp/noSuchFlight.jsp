<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head><title>No Such Flight</title></head>
<body>
<%
    Exception ex = (Exception) request.getAttribute("exception");
%>
<h2>No such flight: <%= ex.getMessage() %>
</h2>
<a href='<c:url value="/flights"/>'>Flights</a>

</body>
</html>