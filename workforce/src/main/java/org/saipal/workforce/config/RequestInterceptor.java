package org.saipal.workforce.config;

import java.util.Arrays;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.saipal.fmisutil.auth.Authenticated;
import org.saipal.fmisutil.parser.RequestParser;
import org.saipal.workforce.lang.AppLangService;
import org.saipal.workforce.service.AuthService;
import org.saipal.workforce.service.AutoService;
import org.saipal.workforce.service.SourceCodeService;
import org.saipal.workforce.service.SystemConfigService;
import org.saipal.workforce.util.FmisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Component
public class RequestInterceptor implements HandlerInterceptor {

	@Autowired
	SystemConfigService service;

	@Autowired
	AuthService aService;

	@Autowired
	SourceCodeService scService;

	private static final Logger LOG = LoggerFactory.getLogger(RequestInterceptor.class);

	@Autowired
	WebApplicationContext context;

	@Autowired
	FmisUtil util;

	@Autowired
	AutoService autoService;

	@Autowired
	AppLangService apls;

	List<String> formIdIgnoreList = Arrays.asList("/list", "/src", "/invoke");

	@Autowired
	RequestParser document;

	@Autowired
	Authenticated auth;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object object) throws Exception {
		// String requestURI = request.getRequestURI();
		// String uri = requestURI;
		// if (!request.getContextPath().isBlank()) {
		// uri = uri.replace(request.getContextPath(), "");
		// }
		// service.logForm(uri);
		// check if the user has permission on the specific menu
		if (auth.getUserId() != null) {
			// if (!aService.checkPermission(requestURI.substring(1))) {
			// response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			// return false;
			// }
		}
		return true;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		// if (modelAndView != null) {
		// if (modelAndView.getViewName() != null) {
		// if (!modelAndView.getViewName().contains("redirect:")) {
		// String baseUrl = ContextHolder.getBaseUrl();
		// modelAndView.addObject("baseUrl", baseUrl);
		// }
		// }
		// }
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
			throws Exception {
		response.setCharacterEncoding("utf-8");
	}

}