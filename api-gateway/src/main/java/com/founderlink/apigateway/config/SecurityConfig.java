package com.founderlink.apigateway.config;

import com.founderlink.apigateway.security.BearerTokenServerAuthenticationConverter;
import com.founderlink.apigateway.security.JwtAuthenticationManager;
import java.util.Arrays;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@Configuration
@EnableReactiveMethodSecurity
public class SecurityConfig {

	/**
	 * Global CORS at the edge only. Downstream services must not emit CORS headers.
	 */
	@Bean
	CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOriginPatterns(List.of("*"));
		configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
		configuration.setAllowedHeaders(List.of("*"));
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}

	@Bean
	SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http,
			JwtAuthenticationManager jwtAuthenticationManager,
			BearerTokenServerAuthenticationConverter bearerTokenConverter,
			CorsConfigurationSource corsConfigurationSource) {
		AuthenticationWebFilter jwtAuthWebFilter = new AuthenticationWebFilter(jwtAuthenticationManager);
		jwtAuthWebFilter.setServerAuthenticationConverter(bearerTokenConverter);
		jwtAuthWebFilter.setSecurityContextRepository(NoOpServerSecurityContextRepository.getInstance());

		return http
				.csrf(csrf -> csrf.disable())
				.cors(cors -> cors.configurationSource(corsConfigurationSource))
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
