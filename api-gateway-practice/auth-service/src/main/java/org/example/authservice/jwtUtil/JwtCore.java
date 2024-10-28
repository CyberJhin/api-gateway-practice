package org.example.authservice.jwtUtil;

import io.jsonwebtoken.*;
import org.example.authservice.service.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtCore {

    @Value("${testing.app.secret}")
    private String accessSecret;

    @Value("${testing.app.refreshSecret}")
    private String refreshSecret;

    @Value("${testing.app.lifetime}")
    private int accessLifetime;

    @Value("${testing.app.refreshLifetime}")
    private int refreshLifetime;

    // Генерация Access Token
    public String generateAccessToken(Authentication authentication) {
        // Получаем объект пользователя из Authentication
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String uuid = userDetails.getUuid();  // Извлекаем uuid пользователя из UserDetailsImpl

        return Jwts.builder()
                .setSubject(uuid)  // Устанавливаем uuid в качестве subject токена
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessLifetime))
                .signWith(SignatureAlgorithm.HS512, accessSecret)
                .compact();
    }

    // Генерация Refresh Token
    public String generateRefreshToken(Authentication authentication) {
        // Получаем объект пользователя из Authentication
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String uuid = userDetails.getUuid();  // Извлекаем uuid пользователя из UserDetailsImpl

        return Jwts.builder()
                .setSubject(uuid)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshLifetime))
                .signWith(SignatureAlgorithm.HS512, refreshSecret)
                .compact();
    }

    // Извлечение UUID из Refresh Token
    public String getUuidFromRefreshToken(String token) {
        return Jwts.parser()
                .setSigningKey(refreshSecret)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();  // Subject теперь хранит uuid
    }

    // Валидация Refresh Token
    public boolean validateRefreshToken(String token) {
        return validateToken(token, refreshSecret);
    }

    public boolean validateAccessToken(String token) {
        return validateToken(token, accessSecret);
    }

    // Общая логика валидации токенов
    private boolean validateToken(String token, String secret) {
        try {
            Jwts.parser().setSigningKey(secret).parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String getUsernameFromAccessToken(String token) {
        return Jwts.parser()
                .setSigningKey(accessSecret)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}
