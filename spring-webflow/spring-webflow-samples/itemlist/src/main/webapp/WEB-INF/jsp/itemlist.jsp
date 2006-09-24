<%@ include file="includeTop.jsp" %>

<div id="content">
	<div id="insert"><img src="../images/webflow-logo.jpg"/></div>
	<h2>Your item list</h2>
	<hr>
	<form action="itemlist" method="post"/>
	<table>
		<tr>
			<td>
				<table border="1" width="300px">
					<c:forEach var="item" items="${list}">
						<tr><td>${item}</td></tr>
					</c:forEach>
				</table>
			</td>
		</tr>
		<tr>
			<td class="buttonBar">
				<!-- Tell webflow what executing flow we're participating in -->
				<input type="hidden" name="_flowExecutionKey" value="${flowExecutionKey}"/>
				<!-- Tell webflow what event happened -->
				<input type="submit" name="_eventId_add" value="Add New Item">
			</td>
		</tr>
	</table>
    </form>
</div>

<%@ include file="includeBottom.jsp" %>