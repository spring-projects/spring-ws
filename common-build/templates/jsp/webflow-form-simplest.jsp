<%@ include file="includeTop.jsp" %>

<div id="content">
	<form action="myapp.htm" method="post"/>
	<table>
		<tr>
			<td colspan="2" class="buttonBar">
				<!-- Tell webflow what executing flow we're participating in -->
				<input type="hidden" name="_flowExecutionId" value="${flowExecutionId}"/>
				<!-- Tell webflow what event happened -->
				<input type="submit" name="_eventId_submit" value="Submit">
			</td>
		</tr>
	</table>
    </form>
</div>

<%@ include file="includeBottom.jsp" %>