package com.example.mediaservice.controller;

import com.example.mediaservice.service.ChannelRedisService;
import com.example.mediaservice.service.UserChannelRedisService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/channel-events")
@AllArgsConstructor
public class ChannelController {


    private final ChannelRedisService channelRedisService;
    private final UserChannelRedisService userChannelRedisService;

    /**
     * Get channel information by ID
     */
    @GetMapping("/{channelId}")
    public ResponseEntity<?> getChannel(@PathVariable String channelId) {
        try {
            Map<String, Object> channel = channelRedisService.getChannel(channelId);
            if (channel == null || channel.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Channel not found in cache");
            }
            return ResponseEntity.ok(channel);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to retrieve channel from cache: " + e.getMessage());
        }
    }

    /**
     * Get all channels for a group
     */
    @GetMapping("/group/{groupId}")
    public ResponseEntity<?> getGroupChannels(@PathVariable String groupId) {
        try {
            Set<Map<String, Object>> channels = channelRedisService.getGroupChannels(groupId);
            return ResponseEntity.ok(channels);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to retrieve group channels from cache: " + e.getMessage());
        }
    }

    /**
     * Get all users in a channel
     */
    @GetMapping("/{channelId}/users")
    public ResponseEntity<?> getChannelUsers(@PathVariable String channelId) {
        try {
            Set<Object> users = userChannelRedisService.getChannelUsers(channelId);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to retrieve channel users from cache: " + e.getMessage());
        }
    }

    /**
     * Get all channels a user is in
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserChannels(@PathVariable String userId) {
        try {
            Set<Object> channels = userChannelRedisService.getUserChannels(userId);
            return ResponseEntity.ok(channels);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to retrieve user channels from cache: " + e.getMessage());
        }
    }

    /**
     * Get user-channel relationship
     */
    @GetMapping("/relationship/{userId}/{channelId}")
    public ResponseEntity<?> getUserChannelRelationship(@PathVariable String userId, @PathVariable String channelId) {
        try {
            Map<String, Object> relationship = userChannelRedisService.getUserChannel(userId, channelId);
            if (relationship == null || relationship.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("User-channel relationship not found");
            }
            return ResponseEntity.ok(relationship);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to retrieve user-channel relationship from cache: " + e.getMessage());
        }
    }

    /**
     * Get default channel for a group
     */
    @GetMapping("/group/{groupId}/default")
    public ResponseEntity<?> getDefaultChannel(@PathVariable String groupId) {
        try {
            Map<String, Object> defaultChannel = channelRedisService.getDefaultChannelForGroup(groupId);
            if (defaultChannel == null || defaultChannel.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Default channel not found for group");
            }
            return ResponseEntity.ok(defaultChannel);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to retrieve default channel from cache: " + e.getMessage());
        }
    }
}

