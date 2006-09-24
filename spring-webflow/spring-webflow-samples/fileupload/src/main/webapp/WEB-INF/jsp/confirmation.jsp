<%@ page session="false" %>

<%@ page import="org.springframework.webflow.samples.fileupload.FileUploadBean" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<HTML>
	<HEAD>
	</HEAD>
	<BODY>
		<FORM name="submitForm" action="admin.htm">
			<INPUT type="hidden" name="_flowExecutionKey" value="<%= request.getAttribute("flowExecutionKey") %>">
			<INPUT type="hidden" name="_eventId" value="back">
		</FORM>
		<DIV align="left">File Contents</DIV>
		<HR>
		<DIV align="left">
			<%
				byte[] file = (byte[])request.getAttribute("file"); 
				if (file != null && file.length > 0) {
			%>
			<TABLE border="1">
				<TR>
					<TD><PRE><%= new String(file) %></PRE></TD>
				</TR>
			</TABLE>
			<%
				}
				else {
			%>
				    No file was uploaded!
			<%
				}
			%>
		</DIV>
		<HR>
		<DIV align="right">
			<INPUT type="button" onclick="javascript:document.submitForm.submit()" value="Back">
		</DIV>
	</BODY>
</HTML>
