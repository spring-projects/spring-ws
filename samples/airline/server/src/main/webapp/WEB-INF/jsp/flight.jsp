<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<html>
<head><title>Flights</title></head>
<body>
<table>
    <tr>
        <td>Number:</td>
        <td>
            <c:out value="${flight.number}"/>
        </td>
    </tr>
    <tr>
        <td>From:</td>
        <td>
            <c:out value="${flight.from.city}"/>
            (
            <c:out value="${flight.from.code}"/>
            )
        </td>
    </tr>
    <tr>
        <td>Departure:</td>
        <td>
            <joda:format value="${flight.departureTime}" style="MM"/>
        </td>
    </tr>
    <tr>
        <td>To:</td>
        <td>
            <c:out value="${flight.to.city}"/>
            (
            <c:out value="${flight.to.code}"/>
            )
        </td>
    </tr>
    <tr>
        <td>Departure:</td>
        <td>
            <joda:format value="${flight.arrivalTime}" style="MM"/>
        </td>
    </tr>
</table>
<a href='<c:url value="/flights.html"/>'>Flights</a>
</body>
</html>