package org.example.userservice.controller;

import org.example.userservice.model.User;
import org.example.userservice.service.UserService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;



    @RestController
    @RequestMapping("/api/users") // Эндпоинт для пользователей
    public class UserController {

        private final UserService userService;

        public UserController(UserService userService) {
            this.userService = userService;
        }

        @GetMapping // Обработка GET-запроса к /api/users
        public ResponseEntity<Map<String, Object>> getUserByUuid(@RequestParam(required = false) String uuid,
                                                                 @RequestHeader HttpHeaders headers) {
            Map<String, Object> response = new HashMap<>();
            response.put("headers", headers.toSingleValueMap());
            response.put("bearerToken", headers.getFirst(HttpHeaders.AUTHORIZATION));
            response.put("uuid", uuid);

            if (uuid != null) {
                User user = userService.findByUuid(uuid);
                if (user != null) {
                    response.put("user", user);
                    return ResponseEntity.ok(response);
                }
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            } else {
                return ResponseEntity.badRequest().body(response); // Возвращаем 400, если UUID не указан
            }
        }
    }


