package com.founderlink.apigateway.config;

import com.founderlink.apigateway.security.BearerTokenServerAuthenticationConverter;
import com.founderlink.apigateway.security.JwtAuthenticationManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;

@Configuration
@EnableReactiveMethodSecurity
public class SecurityConfig {

	@Bean
	SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http,
			JwtAuthenticationManager jwtAuthenticationManager,
			BearerTokenServerAuthenticationConverter bearerTokenConverter) {
		AuthenticationWebFilter jwtAuthWebFilter = new AuthenticationWebFilter(jwtAuthenticationManager);
		jwtAuthWebFilter.setServerAuthenticationConverter(bearerTokenConverter);
		jwtAuthWebFilter.setSecurityContextRepository(NoOpServerSecurityContextRepository.getInstance());

		return http
				.csrf(ServerHttpSecurity.CsrfSpec::disable)
				.httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
				.formLogin(ServerHttpSecurity.FormLoginSpec::disable)
				.securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
				.addFilterAt(jwtAuthWebFilter, SecurityWebFiltersOrder.AUTHENTICATION)
				.authorizeExchange(exchanges -> exchanges
						.pathMatchers("/auth/**").permitAll()
						.pathMatchers("/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**").permitAll()
						.pathMatchers("/users/**").authenticated()
						.pathMatchers(HttpMethod.POST, "/startups/create").hasRole("FOUNDER")
						.pathMatchers("/investments/**").hasRole("INVESTOR")
						.anyExchange().authenticated())
				.build();
	}
}
