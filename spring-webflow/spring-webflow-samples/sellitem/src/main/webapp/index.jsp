<%@ page session="true" %> <%-- make sure we have a session --%>
<HTML>
	<BODY>
		<DIV align="left">Sell Item - A Spring Web Flow Sample</DIV>
		
		<HR>
		
		<DIV align="left">
			<P>
				<A href="pos.htm?_flowId=sellitem">Sell Item</A>
			</P>
			
			<P>
				This Spring Web Flow sample application implements the example application
				discussed in the article
				<A href="http://www-128.ibm.com/developerworks/java/library/j-contin.html">
				Use continuations to develop complex Web applications</A>. It illustrates
				the following concepts:
				<UL>
					<LI>
						Using the "_flowId" request parameter to let the view tell the web
						flow controller which flow needs to be started.
					</LI>
					<LI>
						Implementing a wizard using web flows.
					</LI>
					<LI>
						Using continuations to make the flow completely stable, no matter
						how browser navigation buttons are used.
					</LI>
					<LI>
					    Using "conversation invalidation after completion" to prevent duplicate submits 
					    of the same sale while taking advantage of continuations to allow back button 
					    usage while the application transaction is in process.
					</LI>
					<LI>
						Multi actions to group several action execution methods together on
						a single action implementation class.
					</LI>
					<LI>
						Using <A href="http://www.ognl.org/">OGNL</A> based conditional expressions.
					</LI>
					<LI>
						Exporting the flow registry as a JMX MBean (for access via a standard JMX client
						like JConsole).
					</LI>
					<LI>
						Use of custom flow state exception handlers to recover from exceptions.
					</LI>
				</UL>
			</P>
		</DIV>
		
		<HR>

		<DIV align="right"></DIV>
	</BODY>
</HTML>
