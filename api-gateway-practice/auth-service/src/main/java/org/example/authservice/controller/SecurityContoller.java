package org.example.authservice.controller;

import org.example.authservice.jwtUtil.JwtCore;
import org.example.authservice.model.User;
import org.example.authservice.model.request.SigninRequest;
import org.example.authservice.model.request.SignupRequest;
import org.example.authservice.model.response.AuthResponse;
import org.example.authservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class SecurityContoller {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private AuthenticationManager authenticationManager;
    private JwtCore jwtCore;

    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Autowired
    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Autowired
    public void setJwtCore(JwtCore jwtCore) {
        this.jwtCore = jwtCore;
    }

    @Autowired
    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest signupRequest) {
        if (userRepository.existsByUsername(signupRequest.getUsername())) {
            return ResponseEntity.status(400).body("Username already exists");
        }
        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            return ResponseEntity.status(400).body("Email already exists");
        }

        User user = new User();
        String hashed = passwordEncoder.encode(signupRequest.getPassword());
        user.setPassword(hashed);
        user.setUsername(signupRequest.getUsername());
        user.setEmail(signupRequest.getEmail());
        userRepository.save(user);
        return ResponseEntity.ok("Success");
    }

    @PostMapping("/signin")
    public ResponseEntity<?> signin(@RequestBody SigninRequest signinRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            signinRequest.getUsername(), signinRequest.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            String accessToken = jwtCore.generateAccessToken(authentication);
            String refreshToken = jwtCore.generateRefreshToken(authentication);

            return ResponseEntity.ok(new AuthResponse(accessToken, refreshToken));
        } catch (BadCredentialsException e) {
            return new ResponseEntity<>("Invalid credentials", HttpStatus.UNAUTHORIZED);
        }
    }


    // Метод для обновления Access Token через Refresh Token
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshAccessToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");

        // Проверяем валидность Refresh Token
        if (!jwtCore.validateRefreshToken(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Refresh Token");
        }

        String uuid = jwtCore.getUuidFromRefreshToken(refreshToken);

        // Создаем анонимную аутентификацию для генерации нового Access Token
        Authentication authentication = new UsernamePasswordAuthenticationToken(uuid, null, null);
        String newAccessToken = jwtCore.generateAccessToken(authentication);

        return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
    }

    @PostMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String token) {
        // Извлекаем токен из заголовка Authorization
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);  // Убираем "Bearer "
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token format");
        }

        // Проверяем валидность Access Token
        if (!jwtCore.validateAccessToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
        }

        // Извлекаем имя пользователя из токена
        String username = jwtCore.getUsernameFromAccessToken(token);
        return ResponseEntity.ok(Map.of("uuid", username));
    }
}
