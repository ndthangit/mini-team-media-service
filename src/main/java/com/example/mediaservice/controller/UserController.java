package com.example.mediaservice.controller;

import com.example.mediaservice.service.TokenService;
import com.example.mediaservice.service.UserRedisService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/users")
@AllArgsConstructor
public class UserController {
    private final UserRedisService userRedisService;
    private final TokenService tokenService;

    @GetMapping("/get")
    public ResponseEntity<?> getUserFromRedis() {
        try {
            String email = tokenService.getEmailFromToken();

            if (email == null || email.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Unable to extract email from token");
            }

            Map<String, Object> user = userRedisService.getUser(email);
            if (user == null || user.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("User not found in cache");
            }
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to retrieve user from cache");
        }
    }


}
