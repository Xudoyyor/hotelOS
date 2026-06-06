package com.hotelos.gatewayservice;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class JwtFilter implements WebFilter {

    private final JwtUtil jwtUtil;

    public JwtFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();


        if (path.startsWith("/ws-hotel") || path.startsWith("/api/auth")
                || path.startsWith("/swagger-ui.html")
                || path.startsWith("/swagger-ui/**")
                || path.startsWith("/v3/api-docs/**")
                || path.startsWith("/webjars/**")) {

            return chain.filter(exchange);
        }

        // 2. TOKEN TALAB QILADIGAN YO'LLAR UCHUN
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            try {
                if (!jwtUtil.isExpired(token)) {
                    String username = jwtUtil.extractUsername(token);
                    String role = jwtUtil.extractRole(token);

                    if (role != null && !role.startsWith("ROLE_")) {
                        role = "ROLE_" + role;
                    }

                    SimpleGrantedAuthority authority = new SimpleGrantedAuthority(role);
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(username, null, List.of(authority));

                    return chain.filter(exchange)
                            .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
                }
            } catch (Exception e) {

            }
        }
        return chain.filter(exchange);
    }
}