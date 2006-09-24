<%@ include file="includeTop.jsp" %>

<div id="content">
	<div id="insert"><img src="../images/webflow-logo.jpg"/></div>
	<h2>Add a new item</h2>
	<hr>
	<form action="itemlist" method="post"/>
	<table>
		<tr>
			<td>
				Item:
			</td>
			<td>
				<input type="text" name="data">
			</td>
		</tr>
		<tr>
			<td colspan="2" class="buttonBar">
				<!-- Tell webflow what executing flow we are participating in -->
				<input type="hidden" name="_flowExecutionKey" value="${flowExecutionKey}"/>
				<!-- Tell webflow what event happened -->
				<input type="submit" name="_eventId_submit" value="Submit">
			</td>
		</tr>
	</table>
    </form>
</div>

<%@ include file="includeBottom.jsp" %>