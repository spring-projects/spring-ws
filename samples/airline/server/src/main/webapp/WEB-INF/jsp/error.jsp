<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<%
    Exception ex = (Exception) request.getAttribute("exception");
%>
<head><title><%= ex.getMessage() %>
</title></head>
<body>
<h2><%= ex.getMessage() %>
</h2>
<a href='<c:url value="/flights"/>'>Flights</a>
</body>
</html>