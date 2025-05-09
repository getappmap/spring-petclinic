package org.springframework.samples.petclinic.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

/**
 * Configuration class to register a request logging filter. This filter logs incoming
 * requests for debugging purposes.
 */
@Configuration
public class RequestLoggingFilterConfig {

	@Bean
	public FilterRegistrationBean<CommonsRequestLoggingFilter> loggingFilterRegistration() {
		CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter();
		filter.setIncludeQueryString(true);
		filter.setIncludePayload(true);
		filter.setMaxPayloadLength(10000);
		filter.setIncludeHeaders(false);
		filter.setIncludeClientInfo(false);

		FilterRegistrationBean<CommonsRequestLoggingFilter> registration = new FilterRegistrationBean<>(filter);
		registration.addUrlPatterns("/*");
		registration.setOrder(Ordered.HIGHEST_PRECEDENCE);

		return registration;
	}

}