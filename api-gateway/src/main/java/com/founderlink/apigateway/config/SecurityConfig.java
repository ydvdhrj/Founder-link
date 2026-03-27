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
						// Public: auth & docs
						.pathMatchers("/auth/**").permitAll()
						.pathMatchers("/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**").permitAll()
						.pathMatchers(
								"/auth/v3/api-docs/**",
								"/users/v3/api-docs/**",
								"/startups/v3/api-docs/**",
								"/investments/v3/api-docs/**",
								"/teams/v3/api-docs/**",
								"/messages/v3/api-docs/**",
								"/notifications/v3/api-docs/**").permitAll()
						// User profiles: any authenticated user
						.pathMatchers("/users/**").authenticated()
						// Startups: founders create, everyone authenticated can view
						.pathMatchers(HttpMethod.POST, "/startups", "/startups/**").hasRole("FOUNDER")
						.pathMatchers(HttpMethod.GET, "/startups", "/startups/**").authenticated()
						// Investments: investors create/manage, founders can view their own
						.pathMatchers(HttpMethod.POST, "/investments", "/investments/**").hasRole("INVESTOR")
						.pathMatchers(HttpMethod.PUT, "/investments", "/investments/**").hasRole("FOUNDER")
						.pathMatchers(HttpMethod.GET, "/investments", "/investments/**").hasAnyRole("INVESTOR", "FOUNDER")
						// Teams: founders invite, co-founders join/view
						.pathMatchers(HttpMethod.POST, "/teams", "/teams/**").hasAnyRole("FOUNDER", "COFOUNDER")
						.pathMatchers(HttpMethod.GET, "/teams", "/teams/**").hasAnyRole("FOUNDER", "COFOUNDER")
						// Messaging: any authenticated user
						.pathMatchers("/messages/**").authenticated()
						// Notifications: any authenticated user
						.pathMatchers("/notifications/**").authenticated()
						// Everything else: must be authenticated
						.anyExchange().authenticated())
				.build();
	}
}
