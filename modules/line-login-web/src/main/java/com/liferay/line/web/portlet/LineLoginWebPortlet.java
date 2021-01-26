package com.liferay.line.web.portlet;

import com.liferay.line.web.constants.LineLoginWebPortletKeys;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCPortlet;
import javax.portlet.Portlet;
import org.osgi.service.component.annotations.Component;

/**
 * @author Gongping(GP)-SG
 */
@Component(immediate = true, property = { "com.liferay.portlet.display-category=category.line",
		"com.liferay.portlet.header-portlet-css=/css/main.css", "com.liferay.portlet.instanceable=true",
		"javax.portlet.display-name=LineLoginWeb", "javax.portlet.init-param.template-path=/",
		"javax.portlet.init-param.view-template=/view.jsp",
		"javax.portlet.name=" + LineLoginWebPortletKeys.line_login_web,
		"javax.portlet.resource-bundle=content.Language",
		"javax.portlet.security-role-ref=power-user,user" }, service = Portlet.class)
public class LineLoginWebPortlet extends MVCPortlet {
}