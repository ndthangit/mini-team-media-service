package com.example.mediaservice.service;

import com.example.mediaservice.entity.Group;
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
public class GroupRedisService {

    private static final String GROUP_HASH_KEY = "group";
    private static final String GROUP_CODE_HASH_KEY = "group:code";

    private final RedisTemplate<String, Object> redisTemplate;

    public void saveGroup(Group group) {
        try {
            HashOperations<String, String, Object> hashOps = redisTemplate.opsForHash();
            String groupKey = String.valueOf(group.getId());

            Map<String, Object> groupMap = new HashMap<>();
            groupMap.put("id", String.valueOf(group.getId()));
            groupMap.put("name", group.getName() != null ? group.getName().toString() : null);
            groupMap.put("code", group.getCode() != null ? group.getCode().toString() : null);
            groupMap.put("hidden", String.valueOf(group.getHidden()));
            groupMap.put("createdBy", String.valueOf(group.getCreatedBy()));
            groupMap.put("createdAt", group.getCreatedAt() != null ? String.valueOf(group.getCreatedAt()) : null);

            // Save group by ID
            hashOps.putAll(GROUP_HASH_KEY + ":" + groupKey, groupMap);

            // Save mapping from code to group ID for quick lookup
            if (group.getCode() != null) {
                hashOps.put(GROUP_CODE_HASH_KEY, group.getCode().toString(), groupKey);
            }

            log.info("Saved group to Redis with ID: {} and code: {}", groupKey, group.getCode());
        } catch (Exception e) {
            log.error("Error saving group to Redis: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save group to Redis", e);
        }
    }

    public Map<String, Object> getGroup(Long groupId) {
        try {
            HashOperations<String, String, Object> hashOps = redisTemplate.opsForHash();
            String groupKey = GROUP_HASH_KEY + ":" + groupId;
            Map<String, Object> groupMap = hashOps.entries(groupKey);

            if (groupMap.isEmpty()) {
                log.warn("Group not found in Redis with ID: {}", groupId);
                return null;
            }

            log.info("Retrieved group from Redis with ID: {}", groupId);
            return groupMap;
        } catch (Exception e) {
            log.error("Error retrieving group from Redis: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve group from Redis", e);
        }
    }

    public Map<String, Object> getGroupByCode(String code) {
        try {
            HashOperations<String, String, Object> hashOps = redisTemplate.opsForHash();

            // Get group ID from code mapping
            Object groupIdObj = hashOps.get(GROUP_CODE_HASH_KEY, code);
            if (groupIdObj == null) {
                log.warn("Group not found in Redis with code: {}", code);
                return null;
            }

            String groupId = groupIdObj.toString();
            String groupKey = GROUP_HASH_KEY + ":" + groupId;
            Map<String, Object> groupMap = hashOps.entries(groupKey);

            if (groupMap.isEmpty()) {
                log.warn("Group not found in Redis with ID: {}", groupId);
                return null;
            }

            log.info("Retrieved group from Redis with code: {}", code);
            return groupMap;
        } catch (Exception e) {
            log.error("Error retrieving group by code from Redis: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve group by code from Redis", e);
        }
    }

    public void deleteGroup(Long groupId) {
        try {
            String groupKey = GROUP_HASH_KEY + ":" + groupId;
            redisTemplate.delete(groupKey);
            log.info("Deleted group from Redis with ID: {}", groupId);
        } catch (Exception e) {
            log.error("Error deleting group from Redis: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete group from Redis", e);
        }
    }
}


