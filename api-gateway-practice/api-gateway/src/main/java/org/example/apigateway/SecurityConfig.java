package org.example.apigateway;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.CorsConfiguration;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
                .csrf(ServerHttpSecurity.CsrfSpec::disable) // Отключаем CSRF для API Gateway
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Настраиваем CORS
                .authorizeExchange(exchange -> exchange
                        .pathMatchers(HttpMethod.POST, "/auth/**").permitAll() // Разрешаем доступ к /auth/** для POST запросов
                        .anyExchange().permitAll()); // Обрабатываем неавторизованные запросы


        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.addAllowedOrigin("*"); // Настройка разрешенных доменов; * - разрешает всем
        corsConfig.addAllowedMethod("*"); // Разрешить все методы HTTP
        corsConfig.addAllowedHeader("*"); // Разрешить все заголовки
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);
        return source;
    }
}

