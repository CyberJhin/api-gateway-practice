package org.example.apigateway;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.util.Map;

@Slf4j
@Component
public class GatewayConfig {

    private final WebClient.Builder webClientBuilder;


    public GatewayConfig(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("auth_service_signup", r -> r.path("/auth/signup")
                        .uri("http://localhost:8081"))
                .route("auth_service_signin", r -> r.path("/auth/signin")
                        .uri("http://localhost:8081"))
                .route("auth_service_refresh", r -> r.path("/auth/refresh")
                        .uri("http://localhost:8081"))
                .route("user_service", r -> r.path("/api/users")
                        .filters(f -> f.filter((exchange, chain) -> {
                            log.info("Received request URI: {}", exchange.getRequest().getURI());

                            // Получаем заголовок Authorization
                            String token = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
                            if (token != null && token.startsWith("Bearer ")) {
                                String actualToken = token.substring(7);

                                return validateToken(actualToken)
                                        .flatMap(uuid -> {
                                            if (uuid != null) {
                                                log.info("Token validated, adding UUID to request URI.");

                                                // Создаем новый URI с нужным хостом, портом и параметром UUID
                                                URI newUri = UriComponentsBuilder.fromUri(exchange.getRequest().getURI())
                                                        .scheme("http")
                                                        .host("localhost")
                                                        .port(8082)
                                                        .queryParam("uuid", uuid)
                                                        .build()
                                                        .toUri();

                                                // Создаем измененный запрос с новым URI, сохраняя все заголовки
                                                ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                                                        .uri(newUri)
                                                        .build(); // Не изменяем заголовки

                                                // Создаем измененный exchange с модифицированным запросом
                                                ServerWebExchange mutatedExchange = exchange.mutate()
                                                        .request(mutatedRequest)
                                                        .build();
                                                return chain.filter(mutatedExchange);
                                            } else {
                                                log.warn("Token validation failed, returning 401");
                                                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                                                return exchange.getResponse().setComplete();
                                            }
                                        });
                            } else {
                                log.warn("Authorization token not found, returning 401");
                                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                                return exchange.getResponse().setComplete();
                            }
                        }))
                        .uri("http://localhost:8082")) // URI вашего user-service
                .build();

    }

    private Mono<String> validateToken(String token) {
        return webClientBuilder.build()
                .post()
                .uri("http://localhost:8081/auth/validate")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> {
                    // Handle 4xx errors gracefully
                    System.out.println("LLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLL");
                    System.out.println("Token validation failed with status: " + response.statusCode());
                    log.warn("Token validation failed with status: {}", response.statusCode());
                    return response.createException()
                            .flatMap(Mono::error);
                })
                .bodyToMono(Map.class)
                .doOnNext(response -> log.info("Response from auth service: {}", response))
                .map(response -> {
                    String uuid = (String) response.get("uuid");
                    if (uuid == null) {
                        log.warn("UUID not found in response, token may be invalid");
                        return null; // Return null if UUID not found
                    }
                    log.info("Extracted UUID from auth service: {}", uuid);
                    return uuid; // Return UUID of the user
                });
    }

}
