package com.founderlink.messaging_service.config;

import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class FeignAuthForwardingConfig {

	private static final String AUTHORIZATION = "Authorization";
	private static final String USER_ID = "X-User-Id";

	@Bean
	RequestInterceptor forwardIncomingAuthHeaders() {
		return template -> {
			RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
			if (!(attrs instanceof ServletRequestAttributes servletAttrs)) {
				return;
			}
			HttpServletRequest req = servletAttrs.getRequest();
			String authorization = req.getHeader(AUTHORIZATION);
			if (authorization != null && !authorization.isBlank()) {
				template.header(AUTHORIZATION, authorization);
			}
			String userId = req.getHeader(USER_ID);
			if (userId != null && !userId.isBlank()) {
				template.header(USER_ID, userId);
			}
		};
	}
}
