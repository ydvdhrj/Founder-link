package com.founderlink.apigateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthenticationManager implements ReactiveAuthenticationManager {

	private static final String CLAIM_TYPE = "type";
	private static final String TYPE_ACCESS = "access";

	private final SecretKey signingKey;

	public JwtAuthenticationManager(
			@Value("${jwt.secret:FounderLinkDevSecretKeyMustBeAtLeast32BytesLong!!}") String secret) {
		this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
	}

	@Override
	public Mono<Authentication> authenticate(Authentication authentication) {
		String token = (String) authentication.getCredentials();
		try {
			Claims claims = Jwts.parserBuilder()
					.setSigningKey(signingKey)
					.build()
					.parseClaimsJws(token)
					.getBody();
			String tokenType = claims.get(CLAIM_TYPE, String.class);
			if (!TYPE_ACCESS.equals(tokenType)) {
				return Mono.empty();
			}

			String subject = claims.getSubject();
			Object rolesClaim = claims.get("roles");
			Collection<SimpleGrantedAuthority> authorities = toAuthorities(rolesClaim);

			if (subject == null || subject.isBlank()) {
				return Mono.empty();
			}

			return Mono.just(UsernamePasswordAuthenticationToken.authenticated(subject, token, authorities));
		} catch (JwtException | IllegalArgumentException e) {
			return Mono.empty();
		}
	}

	private Collection<SimpleGrantedAuthority> toAuthorities(Object rolesClaim) {
		if (!(rolesClaim instanceof List<?> roles)) {
			return List.of();
		}
		return roles.stream()
				.filter(String.class::isInstance)
				.map(String.class::cast)
				.map(SimpleGrantedAuthority::new)
				.toList();
	}
}
