package com.liferay.line.web.portlet;

import com.liferay.line.web.constants.LineLoginWebPortletKeys;
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCActionCommand;
import java.io.IOException;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.osgi.service.component.annotations.Component;

@Component(immediate = true, property = { "javax.portlet.name=" + LineLoginWebPortletKeys.line_login_web,
		"mvc.command.name=/login/view" }, service = MVCActionCommand.class)
public class SendMVCRenderCommand implements MVCActionCommand {

	@Override
	public boolean processAction(ActionRequest actionRequest, ActionResponse actionResponse) throws PortletException {

		String url = LineLoginWebPortletKeys.url;
		String response_type = LineLoginWebPortletKeys.response_type;
		String client_id = LineLoginWebPortletKeys.client_id;
		String redirect_uri = LineLoginWebPortletKeys.redirect_uri;
		String state = LineLoginWebPortletKeys.state;
		String scope = LineLoginWebPortletKeys.scope;

		StringBuilder sb = new StringBuilder();

		sb.append(url).append("?").append("response_type=").append(response_type).append("&client_id=")
				.append(client_id).append("&redirect_uri=").append(redirect_uri).append("&state=").append(state)
				.append("&scope=").append(scope);

		String finalUrl = sb.toString();

		try {
			sendHttpGet(finalUrl);
			return true;
		} catch (JSONException | IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	public String sendHttpGet(String url) throws JSONException, IOException {
		HttpClient client = new HttpClient();
		GetMethod method = new GetMethod(url);

		try {
			// Execute the method.
			int statusCode = client.executeMethod(method);

			if (statusCode != HttpStatus.SC_OK) {
				System.err.println("Method failed: " + method.getStatusLine());
			}

			// Read the response body.
			byte[] responseBody = method.getResponseBody();

			// Use caution: ensure correct character encoding and is not binary data
			String result = new String(responseBody);
			return result;
		} catch (HttpException e) {
			System.err.println("Fatal protocol violation: " + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("Fatal transport error: " + e.getMessage());
			e.printStackTrace();
		} finally {
			// Release the connection.
			method.releaseConnection();
		}

		return "";

	}

}
