package com.example.mediaservice.service;

import com.example.mediaservice.entity.relationship.UserChannel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class UserChannelRedisService {

    private static final String USER_CHANNEL_HASH_KEY = "user:channel";
    private static final String CHANNEL_USERS_SET_KEY = "channel:users";
    private static final String USER_CHANNELS_SET_KEY = "user:channels";

    private final RedisTemplate<String, Object> redisTemplate;

    public void saveUserChannel(UserChannel userChannel) {
        try {
            HashOperations<String, String, Object> hashOps = redisTemplate.opsForHash();
            String userChannelKey = userChannel.getUserId() + ":" + userChannel.getChannelId();

            Map<String, Object> userChannelMap = new HashMap<>();
            userChannelMap.put("userId", String.valueOf(userChannel.getUserId()));
            userChannelMap.put("channelId", String.valueOf(userChannel.getChannelId()));
            userChannelMap.put("relationship", String.valueOf(userChannel.getUserChannelRelationship()));

            // Save user-channel relationship
            hashOps.putAll(USER_CHANNEL_HASH_KEY + ":" + userChannelKey, userChannelMap);

            // Add user to channel's user set
            redisTemplate.opsForSet().add(CHANNEL_USERS_SET_KEY + ":" + userChannel.getChannelId(), String.valueOf(userChannel.getUserId()));

            // Add channel to user's channel set
            redisTemplate.opsForSet().add(USER_CHANNELS_SET_KEY + ":" + userChannel.getUserId(), String.valueOf(userChannel.getChannelId()));

            log.info("Saved user-channel relationship to Redis: userId={}, channelId={}", userChannel.getUserId(), userChannel.getChannelId());
        } catch (Exception e) {
            log.error("Error saving user-channel relationship to Redis: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save user-channel relationship to Redis", e);
        }
    }

    public Map<String, Object> getUserChannel(String userId, String channelId) {
        try {
            HashOperations<String, String, Object> hashOps = redisTemplate.opsForHash();
            String userChannelKey = USER_CHANNEL_HASH_KEY + ":" + userId + ":" + channelId;
            Map<String, Object> userChannelMap = hashOps.entries(userChannelKey);

            if (userChannelMap.isEmpty()) {
                log.warn("User-channel relationship not found: userId={}, channelId={}", userId, channelId);
                return null;
            }

            log.info("Retrieved user-channel relationship from Redis: userId={}, channelId={}", userId, channelId);
            return userChannelMap;
        } catch (Exception e) {
            log.error("Error retrieving user-channel relationship from Redis: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve user-channel relationship from Redis", e);
        }
    }

    public Set<Object> getChannelUsers(String channelId) {
        try {
            Set<Object> users = redisTemplate.opsForSet().members(CHANNEL_USERS_SET_KEY + ":" + channelId);
            log.info("Retrieved {} users for channel: {}", users != null ? users.size() : 0, channelId);
            return users;
        } catch (Exception e) {
            log.error("Error retrieving channel users from Redis: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve channel users from Redis", e);
        }
    }

    public Set<Object> getUserChannels(String userId) {
        try {
            Set<Object> channels = redisTemplate.opsForSet().members(USER_CHANNELS_SET_KEY + ":" + userId);
            log.info("Retrieved {} channels for user: {}", channels != null ? channels.size() : 0, userId);
            return channels;
        } catch (Exception e) {
            log.error("Error retrieving user channels from Redis: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve user channels from Redis", e);
        }
    }

    public void deleteUserChannel(String userId, String channelId) {
        try {
            String userChannelKey = USER_CHANNEL_HASH_KEY + ":" + userId + ":" + channelId;
            redisTemplate.delete(userChannelKey);

            // Remove user from channel's user set
            redisTemplate.opsForSet().remove(CHANNEL_USERS_SET_KEY + ":" + channelId, userId);

            // Remove channel from user's channel set
            redisTemplate.opsForSet().remove(USER_CHANNELS_SET_KEY + ":" + userId, channelId);

            log.info("Deleted user-channel relationship from Redis: userId={}, channelId={}", userId, channelId);
        } catch (Exception e) {
            log.error("Error deleting user-channel relationship from Redis: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete user-channel relationship from Redis", e);
        }
    }
}

