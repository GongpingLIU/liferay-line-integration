<%@ include file="/init.jsp"%>

<p>
	<%
		if (themeDisplay.isSignedIn()) {
	%>
		<liferay-ui:message key="Welcome" />
	<%=themeDisplay.getRealUser().getFirstName()%>

	<%
		} else {
	%>
	<a
		href="https://access.line.me/oauth2/v2.1/authorize?response_type=code&client_id=1655547172&redirect_uri=https%3A%2F%2Flocalhost:8443%2Fo%2Fline%2Fline_login&state=z4327Rb3SiXRDo-D0bUD3toQCixSpVyH68jLL9-S6gE&scope=openid%20profile&nonce=SWFEng3EK6E3HNKyIOa-YT8NW5QIrtqEjYpY2sZoLMI">
		Login by Line </a>

	<%
		}
	%>
</p>
