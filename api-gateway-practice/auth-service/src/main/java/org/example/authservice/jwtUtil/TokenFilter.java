package org.example.authservice.jwtUtil;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.authservice.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class TokenFilter extends OncePerRequestFilter {
    private final JwtCore jwtCore;
    private final CustomUserDetailsService userDetailsService;

    @Autowired
    // Внедрение зависимостей через конструктор
    public TokenFilter(JwtCore jwtCore, CustomUserDetailsService userDetailsService) {
        this.jwtCore = jwtCore;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String jwt = extractJwtFromRequest(request);  // Получаем токен из заголовка
        String uuid = null;

        try {
            if (jwt != null) {
                uuid = jwtCore.getUuidFromRefreshToken(jwt);  // Извлечение имени пользователя

                if (uuid != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = userDetailsService.loadUserByUuid(uuid);

                    // Проверяем, что токен валиден для пользователя
                    if (jwtCore.validateAccessToken(jwt)) {
                        UsernamePasswordAuthenticationToken auth =
                                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                        SecurityContextHolder.getContext().setAuthentication(auth);
                    }
                }
            }
        } catch (ExpiredJwtException e) {
            // Обрабатываем истекший токен
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Access token expired. Please refresh.");
            return;  // Прерываем фильтрацию, если токен истек
        } catch (SignatureException e) {
            // Обработка ошибок подписи JWT
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid token signature.");
            return;  // Прерываем фильтрацию, если токен недействителен
        } catch (JwtException e) {
            // Обработка других ошибок JWT
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid token.");
            return;  // Прерываем фильтрацию
        } catch (Exception e) {
            // Логируем любые другие ошибки
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("An error occurred while processing the token.");
            return;  // Прерываем фильтрацию
        }

        filterChain.doFilter(request, response);  // Продолжаем выполнение цепочки фильтров
    }

    // Извлекаем JWT из заголовка Authorization
    private String extractJwtFromRequest(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        if (headerAuth != null && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);  // Убираем "Bearer "
        }
        return null;
    }
}
