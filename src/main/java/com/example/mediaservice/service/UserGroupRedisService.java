package com.example.mediaservice.service;

import com.example.mediaservice.entity.relationship.UserGroup;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
@AllArgsConstructor
@Slf4j
public class UserGroupRedisService {

    private static final String USER_GROUP_HASH_KEY = "user:group";
    private static final String GROUP_USERS_SET_KEY = "group:users";

    private final RedisTemplate<String, Object> redisTemplate;

    public void saveUserGroup(UserGroup userGroup) {
        try {
            HashOperations<String, String, Object> hashOps = redisTemplate.opsForHash();
            SetOperations<String, Object> setOps = redisTemplate.opsForSet();

            String userGroupKey = userGroup.getUserId() + ":" + userGroup.getGroupId();

            Map<String, Object> userGroupMap = new HashMap<>();
            userGroupMap.put("userId", String.valueOf(userGroup.getUserId()));
            userGroupMap.put("groupId", String.valueOf(userGroup.getGroupId()));
            userGroupMap.put("relationshipType", userGroup.getRelationshipType().toString());

            // Save user-group relationship
            hashOps.putAll(USER_GROUP_HASH_KEY + ":" + userGroupKey, userGroupMap);

            // Add user to group's user set
            setOps.add(GROUP_USERS_SET_KEY + ":" + userGroup.getGroupId(), String.valueOf(userGroup.getUserId()));

            log.info("Saved user-group relationship to Redis - User: {}, Group: {}, Type: {}",
                    userGroup.getUserId(), userGroup.getGroupId(), userGroup.getRelationshipType());
        } catch (Exception e) {
            log.error("Error saving user-group relationship to Redis: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save user-group relationship to Redis", e);
        }
    }

    public Map<String, Object> getUserGroup(Long userId, Long groupId) {
        try {
            HashOperations<String, String, Object> hashOps = redisTemplate.opsForHash();
            String userGroupKey = USER_GROUP_HASH_KEY + ":" + userId + ":" + groupId;
            Map<String, Object> userGroupMap = hashOps.entries(userGroupKey);

            if (userGroupMap.isEmpty()) {
                log.warn("User-group relationship not found in Redis - User: {}, Group: {}", userId, groupId);
                return null;
            }

            log.info("Retrieved user-group relationship from Redis - User: {}, Group: {}", userId, groupId);
            return userGroupMap;
        } catch (Exception e) {
            log.error("Error retrieving user-group relationship from Redis: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve user-group relationship from Redis", e);
        }
    }

    public Set<Object> getGroupUsers(Long groupId) {
        try {
            SetOperations<String, Object> setOps = redisTemplate.opsForSet();
            String groupUsersKey = GROUP_USERS_SET_KEY + ":" + groupId;
            Set<Object> users = setOps.members(groupUsersKey);

            log.info("Retrieved {} users for group {} from Redis", users != null ? users.size() : 0, groupId);
            return users;
        } catch (Exception e) {
            log.error("Error retrieving group users from Redis: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve group users from Redis", e);
        }
    }

    public void deleteUserGroup(Long userId, Long groupId) {
        try {
            HashOperations<String, String, Object> hashOps = redisTemplate.opsForHash();
            SetOperations<String, Object> setOps = redisTemplate.opsForSet();

            String userGroupKey = USER_GROUP_HASH_KEY + ":" + userId + ":" + groupId;
            redisTemplate.delete(userGroupKey);

            // Remove user from group's user set
            setOps.remove(GROUP_USERS_SET_KEY + ":" + groupId, String.valueOf(userId));

            log.info("Deleted user-group relationship from Redis - User: {}, Group: {}", userId, groupId);
        } catch (Exception e) {
            log.error("Error deleting user-group relationship from Redis: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete user-group relationship from Redis", e);
        }
    }
}

