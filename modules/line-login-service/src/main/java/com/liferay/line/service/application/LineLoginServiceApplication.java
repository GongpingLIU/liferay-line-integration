package com.liferay.line.service.application;

import com.liferay.line.service.constants.LineLoginServiceKeys;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.CompanyConstants;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.security.auth.session.AuthenticatedSessionManagerUtil;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.jaxrs.whiteboard.JaxrsWhiteboardConstants;

/**
 * @author Gongping(GP)-SG
 */
@Component(property = { JaxrsWhiteboardConstants.JAX_RS_APPLICATION_BASE + "=/line",
		JaxrsWhiteboardConstants.JAX_RS_NAME + "=LineLogin" }, service = Application.class)
public class LineLoginServiceApplication extends Application {

	Log logger = LogFactoryUtil.getLog(LineLoginServiceApplication.class);

	private static final String client_id = LineLoginServiceKeys.client_id;
	private static final String grant_type = LineLoginServiceKeys.grant_type;
	private static final String client_secret = LineLoginServiceKeys.client_secret;
	private static final String redirect_url = LineLoginServiceKeys.redirect_url;
	private static final long id = LineLoginServiceKeys.id;
	private static final long company_id = LineLoginServiceKeys.company_id;
	private static final String last = LineLoginServiceKeys.last;
	private static final String default_password = LineLoginServiceKeys.default_password;
	private static final String redirectUrl = LineLoginServiceKeys.redirectUrl;

	private class AccessTokenInfo {
		String id_token;
	}

	private class UserInfo {
		String name;
	}

	public Set<Object> getSingletons() {
		return Collections.<Object>singleton(this);
	}

	@GET
	@Path("/line_login")
	@Produces("text/plain")
	public String lineLogin(@Context HttpServletRequest request, @Context HttpServletResponse response)
			throws Exception {

		response.setCharacterEncoding("utf-8");
		String code = request.getParameter("code");
		AccessTokenInfo accessToken = getAccessToken(code);

		UserInfo userInfo = getUserInfo(accessToken);

		User user = null;
		boolean newAdded = false;

		try {
			user = UserLocalServiceUtil.getUserByScreenName(company_id, userInfo.name);
		} catch (PortalException e) {
			user = addUser(userInfo.name);
			newAdded = true;
		}

		String password = user.getPassword();

		password = password.substring(6);

		if (newAdded) {
			AuthenticatedSessionManagerUtil.login(request, response, user.getScreenName(), default_password, true,
					CompanyConstants.AUTH_TYPE_SN);

		} else {

			AuthenticatedSessionManagerUtil.login(request, response, user.getScreenName(), password, true,
					CompanyConstants.AUTH_TYPE_SN);
		}

		response.sendRedirect(redirectUrl);
		return "done.";
	}

	private AccessTokenInfo getAccessToken(String code) throws JSONException, IOException {

		String url = "https://api.line.me/oauth2/v2.1/token";
		String redirect_uri = "https%3A%2F%2Flocalhost:8443%2Fo%2Fline%2Fline_login";

		StringBuilder sb = new StringBuilder();
		sb.append(url).append("?").append("client_id=").append(client_id).append("&client_secret=")
				.append(client_secret).append("&code=").append(code).append("&grant_type=").append(grant_type)
				.append("&redirect_uri=").append(redirect_uri);

		String finalUrl = sb.toString();
		String result = sendHttpPost(finalUrl, code);

		JSONObject jsonObject = JSONFactoryUtil.createJSONObject(result);
		String id_token = jsonObject.getString("id_token");

		AccessTokenInfo info = new AccessTokenInfo();
		info.id_token = id_token;

		return info;
	}

	private UserInfo getUserInfo(AccessTokenInfo info) throws JSONException, IOException {

		String token = info.id_token;

		String[] split_string = token.split("\\.");
		String encode = split_string[1];

		Base64 base64Url = new Base64(true);
		String body = new String(base64Url.decode(encode));

		JSONObject jsonObject = JSONFactoryUtil.createJSONObject(body);

		UserInfo userInfo = new UserInfo();
		userInfo.name = jsonObject.getString("name");

		return userInfo;
	}

	private User addUser(String name) throws PortalException {

		long[] empty_long_list = {};

		// login as the screen name
		User user = UserLocalServiceUtil.addUser(id, company_id, false, default_password, default_password, false, name,
				name + "@liferay.com", 0, "", java.util.Locale.CHINA, name, "", last, 0, 0, true, 1, 1, 1970, "",
				empty_long_list, empty_long_list, empty_long_list, empty_long_list, false, new ServiceContext());

		return user;
	}

	public String sendHttpPost(String url, String code) throws JSONException, IOException {

		HttpClient client = new HttpClient();
		logger.info(url);
		PostMethod method = new PostMethod(url);
		method.setParameter("grant_type", grant_type);
		method.setParameter("client_id", client_id);
		method.setParameter("client_secret", client_secret);
		method.setParameter("code", code);
		method.setParameter("redirect_uri", redirect_url);

		try {
			// Execute the method.
			client.executeMethod(method);

			// Read the response body.
			byte[] responseBody = method.getResponseBody();

			// Use caution: ensure correct character encoding and is not binary data
			String result = new String(responseBody, "utf-8");

			return result;
		} catch (HttpException e) {
			logger.error("Fatal protocol violation: " + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			logger.error("Fatal transport error: " + e.getMessage());
			e.printStackTrace();
		} finally {
			// Release the connection.
			method.releaseConnection();
		}

		return "";
	}

}