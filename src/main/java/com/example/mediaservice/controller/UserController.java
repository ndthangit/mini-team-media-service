package com.example.mediaservice.controller;

import com.example.mediaservice.service.UserRedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    private UserRedisService userRedisService;

    @GetMapping("/{email}")
    public ResponseEntity<?> getUserFromRedis(@PathVariable String email) {
        try {
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
