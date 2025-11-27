package com.example.mediaservice.service;

import com.example.mediaservice.entity.User;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@AllArgsConstructor
@Slf4j
public class UserRedisService {

    private static final String USER_HASH_KEY = "user";

    private final RedisTemplate<String, Object> redisTemplate;

    public void saveUser(User user) {
        try {
            HashOperations<String, String, Object> hashOps = redisTemplate.opsForHash();
            String userKey = String.valueOf(user.getEmail());
            
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("email", String.valueOf(user.getEmail()));

            userMap.put("dateOfBirth", user.getDateOfBirth() != null ? String.valueOf(user.getDateOfBirth()) : null);
            userMap.put("gender", user.getGender() != null ? String.valueOf(user.getGender()) : null);
            userMap.put("firstName", user.getFirstName() != null ? String.valueOf(user.getFirstName()) : null);
            userMap.put("lastName", user.getLastName() != null ? String.valueOf(user.getLastName()) : null);
            userMap.put("occupation", user.getOccupation() != null ? String.valueOf(user.getOccupation()) : null);

            hashOps.putAll(USER_HASH_KEY + ":" + userKey, userMap);
            log.info("Saved user to Redis with key: {}", userKey);
        } catch (Exception e) {
            log.error("Error saving user to Redis: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save user to Redis", e);
        }
    }

    public Map<String, Object> getUser(String email) {
        try {
            HashOperations<String, String, Object> hashOps = redisTemplate.opsForHash();
            String userKey = USER_HASH_KEY + ":" + email;
            Map<String, Object> userMap = hashOps.entries(userKey);
            
            if (userMap.isEmpty()) {
                log.warn("User not found in Redis with email: {}", email);
                return null;
            }
            
            log.info("Retrieved user from Redis with email: {}", email);
            return userMap;
        } catch (Exception e) {
            log.error("Error retrieving user from Redis: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve user from Redis", e);
        }
    }

    public void deleteUser(String email) {
        try {
            String userKey = USER_HASH_KEY + ":" + email;
            redisTemplate.delete(userKey);
            log.info("Deleted user from Redis with email: {}", email);
        } catch (Exception e) {
            log.error("Error deleting user from Redis: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete user from Redis", e);
        }
    }
}
