package com.hotelos.gatewayservice;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
                // CSRF va CORS himoyalarini reaktiv zanjirda mutloq o'chiramiz
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(ServerHttpSecurity.CorsSpec::disable)

                // CRITICAL: Brauzerda login/parol oynasi chiqishini mutloq taqiqlaymiz
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)

                .authorizeExchange(exchanges -> exchanges
                        // Ochiq manzillar (Hech qanday token yoki login talab qilinmaydi)
                        .pathMatchers("/ws-hotel/**", "/api/auth/**").permitAll()
                        .pathMatchers("/swagger-ui.html", "/swagger-ui/**",
                                "/v3/api-docs/**", "/webjars/**").permitAll()
                        .pathMatchers("/api/*/v3/api-docs").permitAll()

                        // Himoyalangan API'lar
                        .pathMatchers("/api/room-service/**").authenticated()
                        .pathMatchers("/api/maintenance/**").authenticated()
                        .pathMatchers("/api/reception/**").authenticated()
                        .pathMatchers("/api/housekeeping/**").authenticated()

                        // Qolgan barcha so'rovlar himoyalangan
                        .anyExchange().authenticated()
                )
                // O'zingizning shaxsiy JWT filtringizni autentifikatsiyadan oldin qo'shish
                .addFilterBefore(jwtFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowCredentials(true);
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsWebFilter(source);
    }
}