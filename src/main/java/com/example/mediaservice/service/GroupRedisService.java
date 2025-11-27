package com.example.mediaservice.service;

import com.example.mediaservice.entity.Group;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GroupRedisService {

    private static final String USER_GROUPS_KEY_PREFIX = "user:";
    private static final String USER_GROUPS_KEY_SUFFIX = ":groups";

    // Assuming RedisTemplate is configured for <String, String>
    // If it's <String, Object>, casting or serializer adjustments might be needed.
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    private String getUserGroupsKey(String userEmail) {
        return USER_GROUPS_KEY_PREFIX + userEmail + USER_GROUPS_KEY_SUFFIX;
    }

    /**
     * Adds or updates a group for a specific user using HSET.
     * The group object is stored as a JSON string.
     *
     * @param userEmail The user's email, used to build the Redis key.
     * @param group     The group to save. Its ID will be the hash field.
     */
    public void addGroupToUser(String userEmail, Group group) {
        String key = getUserGroupsKey(userEmail);
        String field = String.valueOf(group.getId());
        try {
            String value = objectMapper.writeValueAsString(group);
            HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();
            hashOps.put(key, field, value);
            log.info("Added/updated group with ID {} for user '{}'", field, userEmail);
        } catch (JsonProcessingException e) {
            log.error("Error serializing group to JSON for user '{}': {}", userEmail, e.getMessage(), e);
            throw new RuntimeException("Failed to serialize group for Redis", e);
        } catch (Exception e) {
            log.error("Error saving group to Redis for user '{}': {}", userEmail, e.getMessage(), e);
            throw new RuntimeException("Failed to save group to Redis", e);
        }
    }

    /**
     * Checks if a group exists in the user's hash using HEXISTS. O(1) complexity.
     *
     * @param userEmail The user's email.
     * @param groupId   The ID of the group to check.
     * @return True if the user is in the group, false otherwise.
     */
    public boolean isUserInGroup(String userEmail, String groupId) {
        String key = getUserGroupsKey(userEmail);
        try {
            HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();
            return hashOps.hasKey(key, groupId);
        } catch (Exception e) {
            log.error("Error checking group existence in Redis for user '{}': {}", userEmail, e.getMessage(), e);
            throw new RuntimeException("Failed to check group existence in Redis", e);
        }
    }

    /**
     * Retrieves all groups for a specific user using HVALS.
     *
     * @param userEmail The user's email.
     * @return A list of Group objects.
     */
    public List<Group> getGroupsByUser(String userEmail) {
        String key = getUserGroupsKey(userEmail);
        try {
            HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();
            List<String> groupJsonList = hashOps.values(key);

            return groupJsonList.stream()
                    .map(json -> {
                        try {
                            return objectMapper.readValue(json, Group.class);
                        } catch (IOException e) {
                            log.error("Error deserializing group JSON for user '{}': {}", userEmail, e.getMessage());
                            return null; // Skip corrupted data
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error retrieving groups from Redis for user '{}': {}", userEmail, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve groups from Redis", e);
        }
    }

    /**
     * Removes a specific group from a user's hash using HDEL.
     *
     * @param userEmail The user's email.
     * @param groupId   The ID of the group to remove.
     */
    public void removeGroupFromUser(String userEmail, String groupId) {
        String key = getUserGroupsKey(userEmail);
        try {
            HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();
            hashOps.delete(key, groupId);
            log.info("Deleted group with ID {} for user '{}'", groupId, userEmail);
        } catch (Exception e) {
            log.error("Error deleting group from Redis for user '{}': {}", userEmail, e.getMessage(), e);
            throw new RuntimeException("Failed to delete group from Redis", e);
        }
    }
}
