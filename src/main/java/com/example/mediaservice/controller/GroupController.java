package com.example.mediaservice.controller;

import com.example.mediaservice.entity.Group;
import com.example.mediaservice.entity.relationship.UserChannel;
import com.example.mediaservice.entity.relationship.UserGroup;
import com.example.mediaservice.producer.ChannelProducerService;
import com.example.mediaservice.producer.GroupProducerService;
import com.example.mediaservice.producer.UserChannelProducerService;
import com.example.mediaservice.producer.UserGroupProducerService;
import com.example.mediaservice.service.ChannelRedisService;
import com.example.mediaservice.service.GroupRedisService;
import com.example.mediaservice.service.TokenService;
import com.example.mediaservice.service.UserGroupRedisService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/group")
@AllArgsConstructor
public class GroupController {

    private final UserGroupProducerService userGroupProducerService;
    private final UserChannelProducerService userChannelProducerService;
    private final GroupRedisService groupRedisService;
    private final UserGroupRedisService userGroupRedisService;
    private final ChannelRedisService channelRedisService;
    private final TokenService tokenService;

    @PostMapping("/join/{groupId}")
    public ResponseEntity<String> joinGroup(@PathVariable String groupId) {
        try {
            String userEmail = tokenService.getEmailFromToken();

            if (userEmail == null || userEmail.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Unable to extract email from token");
            }

            // Check if user is already in the group
            if (groupRedisService.isUserInGroup(userEmail, groupId)) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("User is already a member of this group");
            }

            // Create UserGroup relationship event
            UserGroup userGroup = UserGroup.newBuilder()
                    .setUserId(userEmail)
                    .setGroupId(groupId)
                    .setUserGroupRelationship(com.example.mediaservice.entity.relationship.UserGroupRelationship.JOIN)
                    .build();

            // Send user-group-join event
            userGroupProducerService.sendUserGroupEvent(userGroup);

            // Find the default "general" channel for this group
            Map<String, Object> defaultChannel = channelRedisService.getDefaultChannelForGroup(groupId);
            if (defaultChannel != null && !defaultChannel.isEmpty()) {
                String channelId = defaultChannel.get("channelId").toString();

                // Add user to the general channel
                UserChannel userChannel = UserChannel.newBuilder()
                        .setUserId(userEmail)
                        .setChannelId(channelId)
                        .setUserChannelRelationship(com.example.mediaservice.entity.relationship.UserChannelRelationship.JOIN)
                        .build();

                // Send user-channel event
                userChannelProducerService.sendUserChannelEvent(userChannel);

                return ResponseEntity.accepted().body("Group join request accepted. User: " + userEmail + ", Group: " + groupId + ", added to 'general' channel");
            } else {
                return ResponseEntity.accepted().body("Group join request accepted. User: " + userEmail + ", Group: " + groupId + " (warning: no default channel found)");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to process group join request: " + e.getMessage());
        }
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<?> getGroup(@PathVariable String groupId) {
        try {
            String userEmail = tokenService.getEmailFromToken();

            if (userEmail == null || userEmail.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Unable to extract email from token");
            }


            List<Group> groups = groupRedisService.getGroupsByUser(userEmail);
            Optional<Group> group = groups.stream()
                    .filter(g -> g.getId().toString().equals(groupId))
                    .findFirst();

            if (group.isPresent()) {
                return ResponseEntity.ok(group.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Group not found for this user in cache");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to retrieve group from cache: " + e.getMessage());
        }
    }

    @GetMapping("/my-groups")
    public ResponseEntity<?> getMyGroups() {
        try {
            String userEmail = tokenService.getEmailFromToken();

            if (userEmail == null || userEmail.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Unable to extract email from token");
            }


            List<Group> groups = groupRedisService.getGroupsByUser(userEmail);
            return ResponseEntity.ok(groups);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to retrieve groups from cache: " + e.getMessage());
        }
    }

    @GetMapping("/{groupId}/users")
    public ResponseEntity<?> getGroupUsers(@PathVariable String groupId) {
        try {
            Set<Object> users = userGroupRedisService.getGroupUsers(groupId);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to retrieve group users from cache: " + e.getMessage());
        }
    }

    @GetMapping("/relationship/{userId}/{groupId}")
    public ResponseEntity<?> getUserGroupRelationship(@PathVariable String userId, @PathVariable String groupId) {
        try {
            Map<String, Object> relationship = userGroupRedisService.getUserGroup(userId, groupId);
            if (relationship == null || relationship.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("User-group relationship not found");
            }
            return ResponseEntity.ok(relationship);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to retrieve user-group relationship from cache: " + e.getMessage());
        }
    }
}
