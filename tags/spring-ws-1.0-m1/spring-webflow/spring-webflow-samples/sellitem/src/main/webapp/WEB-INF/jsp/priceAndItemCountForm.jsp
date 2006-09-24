<%@ include file="includeTop.jsp" %>

<div id="content">
	<div id="insert"><img src="images/webflow-logo.jpg"/></div>
	<h2>Enter price and item count</h2>
	<hr>
	<table>
		<form name="priceAndItemCountForm" method="post">
		<spring:nestedPath path="sale">
		<tr>
			<td>Price:</td>
			<td>
				<spring:bind path="price">
					<input type="text" name="${status.expression}" value="${status.value}">
					<c:if test="${status.error}">
						<div class="error">${status.errorMessage}</div>
					</c:if>
				</spring:bind>
			</td>
		</tr>		
		<tr>
			<td>Item count:</td>
			<td>
				<spring:bind path="itemCount">
					<input type="text" name="${status.expression}" value="${status.value}">
					<c:if test="${status.error}">
						<div class="error">${status.errorMessage}</div>
					</c:if>
				</spring:bind>
			</td>
		</tr>
		<tr>
			<td colspan="2" class="buttonBar">
				<input type="hidden" name="_flowExecutionKey" value="${flowExecutionKey}">
				<input type="submit" class="button" name="_eventId_submit" value="Next">
				<input type="submit" class="button" name="_eventId_preview" value="Preview Sale">
			</td>
		</tr>
		</spring:nestedPath>
		</form>
	</table>
</div>

<%@ include file="includeBottom.jsp" %>