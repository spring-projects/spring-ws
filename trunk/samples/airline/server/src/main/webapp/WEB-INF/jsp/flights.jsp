<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<html>
<head><title>Flights</title></head>
<body>
<form a method="get" action='<c:url value="/flights.html"/>'>
    <table>
        <tr>
            <td>From:</td>
            <td><input name="from" value='<c:out value="${from}" default="AMS"/>'/></td>
        </tr>
        <tr>
            <td>To:</td>
            <td><input name="to" value="<c:out value="${to}" default="VCE"/>"/></td>
        </tr>
        <tr>
            <td>Departure Date:</td>
            <td><input name="departureDate" value="<c:out value="${departureDate}" default="2006-01-31"/>"/></td>
        </tr>
        <tr>
            <td>Service Class:</td>
            <td>
                <select name="serviceClass">
                    <option value="ECONOMY" selected="selected">Economy</option>
                    <option value="BUSINESS">Business</option>
                    <option value="FIRST">First</option>
                </select>
            </td>
        </tr>
        <tr>
            <td colspan="2">
                <input type="submit" value="Submit"/>
            </td>
        </tr>
    </table>
    <c:if test="${!empty flights}">
        <table border="1px">
            <tr>
                <th>Number</th>
                <th>Departs</th>
                <th>Arrives</th>
            </tr>
            <c:forEach var="flight" items="${flights}">
                <tr>
                    <td>
                        <spring:url var="flightUrl" value="flights/{id}.html">
                            <spring:param name="id" value="${flight.id}"/>
                        </spring:url>
                        <a href='<c:out value="${flightUrl}"/>'><c:out value="${flight.number}"/></a>
                    </td>
                    <td>
                        <c:out value="${flight.from.city}"/>
                        (
                        <c:out value="${flight.from.code}"/>
                        )
                        <joda:format value="${flight.departureTime}" style="SS"/>
                    </td>
                    <td>
                        <c:out value="${flight.to.city}"/>
                        (
                        <c:out value="${flight.to.code}"/>
                        )
                        <joda:format value="${flight.arrivalTime}" style="SS"/>
                    </td>
                </tr>
            </c:forEach>
        </table>
    </c:if>
</form>
</body>
</html>