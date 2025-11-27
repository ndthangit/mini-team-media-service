package com.example.mediaservice.service;

import com.example.mediaservice.entity.Channel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
@Service
public class ChannelRedisService {

    private static final String CHANNEL_HASH_KEY = "channel";
    private static final String GROUP_CHANNELS_SET_KEY = "group:channels";

    private final RedisTemplate<String, Object> redisTemplate;

    public void saveChannel(Channel channel) {
        try {
            HashOperations<String, String, Object> hashOps = redisTemplate.opsForHash();
            String channelKey = String.valueOf(channel.getChannelId());

            Map<String, Object> channelMap = new HashMap<>();
            channelMap.put("channelId", String.valueOf(channel.getChannelId()));
            channelMap.put("name", channel.getName() != null ? channel.getName().toString() : null);
            channelMap.put("groupId", String.valueOf(channel.getGroupId()));
            // Mark as default if name is "general"
            if ("general".equals(channel.getName().toString())) {
                channelMap.put("isDefault", "true");
            }

            // Save channel by ID
            hashOps.putAll(CHANNEL_HASH_KEY + ":" + channelKey, channelMap);

            // Add channel ID to the group's channel set
            redisTemplate.opsForSet().add(GROUP_CHANNELS_SET_KEY + ":" + channel.getGroupId(), channelKey);

            log.info("Saved channel to Redis with ID: {} for group: {}", channelKey, channel.getGroupId());
        } catch (Exception e) {
            log.error("Error saving channel to Redis: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save channel to Redis", e);
        }
    }

    public Map<String, Object> getChannel(String channelId) {
        try {
            HashOperations<String, String, Object> hashOps = redisTemplate.opsForHash();
            String channelKey = CHANNEL_HASH_KEY + ":" + channelId;
            Map<String, Object> channelMap = hashOps.entries(channelKey);

            if (channelMap.isEmpty()) {
                log.warn("Channel not found in Redis with ID: {}", channelId);
                return null;
            }

            log.info("Retrieved channel from Redis with ID: {}", channelId);
            return channelMap;
        } catch (Exception e) {
            log.error("Error retrieving channel from Redis: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve channel from Redis", e);
        }
    }

    public Set<Map<String, Object>> getGroupChannels(String groupId) {
        try {
            // Get all channel IDs for the group
            Set<Object> channelIds = redisTemplate.opsForSet().members(GROUP_CHANNELS_SET_KEY + ":" + groupId);

            if (channelIds == null || channelIds.isEmpty()) {
                log.warn("No channels found for group: {}", groupId);
                return Set.of();
            }

            // Retrieve details for each channel
            return channelIds.stream()
                    .map(channelId -> getChannel(channelId.toString()))
                    .filter(channel -> channel != null)
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            log.error("Error retrieving group channels from Redis: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve group channels from Redis", e);
        }
    }

    public Map<String, Object> getDefaultChannelForGroup(String groupId) {
        try {
            Set<Map<String, Object>> channels = getGroupChannels(groupId);
            return channels.stream()
                    .filter(channel -> "true".equals(channel.get("isDefault")))
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            log.error("Error retrieving default channel for group: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve default channel", e);
        }
    }

    public void deleteChannel(String channelId) {
        try {
            Map<String, Object> channel = getChannel(channelId);
            if (channel != null) {
                String groupId = channel.get("groupId").toString();
                // Remove from group's channel set
                redisTemplate.opsForSet().remove(GROUP_CHANNELS_SET_KEY + ":" + groupId, channelId);
            }

            String channelKey = CHANNEL_HASH_KEY + ":" + channelId;
            redisTemplate.delete(channelKey);
            log.info("Deleted channel from Redis with ID: {}", channelId);
        } catch (Exception e) {
            log.error("Error deleting channel from Redis: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete channel from Redis", e);
        }
    }
}
