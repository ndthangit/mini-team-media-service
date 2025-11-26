package com.example.mediaservice.controller;

import com.example.mediaservice.dto.CreateGroupRequest;
import com.example.mediaservice.dto.JoinGroupRequest;
import com.example.mediaservice.entity.Group;
import com.example.mediaservice.entity.relationship.RelationshipType;
import com.example.mediaservice.entity.relationship.UserGroup;
import com.example.mediaservice.producer.GroupProducerService;
import com.example.mediaservice.producer.UserGroupProducerService;
import com.example.mediaservice.service.GroupRedisService;
import com.example.mediaservice.service.UserGroupRedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Random;
import java.util.Set;

@RestController
@RequestMapping("/group-events")
public class GroupController {

    @Autowired
    private GroupProducerService groupProducerService;

    @Autowired
    private UserGroupProducerService userGroupProducerService;

    @Autowired
    private GroupRedisService groupRedisService;

    @Autowired
    private UserGroupRedisService userGroupRedisService;

    /**
     * Create a new group and establish user-group relationship
     */
    @PostMapping("/create")
    public ResponseEntity<String> createGroup(@RequestBody CreateGroupRequest request) {
        try {
            // Generate unique group ID and code
            Long groupId = System.currentTimeMillis();
            String groupCode = generateGroupCode();

            // Create Group event
            Group group = Group.newBuilder()
                    .setId(groupId)
                    .setName(request.getName())
                    .setCode(groupCode)
                    .setHidden(request.isHidden())
                    .setCreatedBy(request.getUserId())
                    .setCreatedAt(System.currentTimeMillis())
                    .build();

            // Send group-created event
            groupProducerService.sendGroupCreated(group);

            // Create UserGroup relationship event
            UserGroup userGroup = UserGroup.newBuilder()
                    .setUserId(request.getUserId())
                    .setGroupId(groupId)
                    .setRelationshipType(RelationshipType.CREATE)
                    .build();

            // Send user-group-create event
            userGroupProducerService.sendUserGroupEvent(userGroup);

            return ResponseEntity.accepted().body("Group creation request accepted. Group ID: " + groupId + ", Code: " + groupCode);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to process group creation request: " + e.getMessage());
        }
    }

    /**
     * Join a group using group code
     */
    @PostMapping("/join")
    public ResponseEntity<String> joinGroup(@RequestBody JoinGroupRequest request) {
        try {
            // Check if group exists by code
            Map<String, Object> group = groupRedisService.getGroupByCode(request.getGroupCode());
            if (group == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Group not found with code: " + request.getGroupCode());
            }

            Long groupId = Long.parseLong(group.get("id").toString());

            // Check if user is already in the group
            Map<String, Object> existingRelationship = userGroupRedisService.getUserGroup(request.getUserId(), groupId);
            if (existingRelationship != null) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("User is already a member of this group");
            }

            // Create UserGroup relationship event
            UserGroup userGroup = UserGroup.newBuilder()
                    .setUserId(request.getUserId())
                    .setGroupId(groupId)
                    .setRelationshipType(RelationshipType.JOIN)
                    .build();

            // Send user-group-join event
            userGroupProducerService.sendUserGroupEvent(userGroup);

            return ResponseEntity.accepted().body("Group join request accepted. User: " + request.getUserId() + ", Group: " + groupId);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to process group join request: " + e.getMessage());
        }
    }

    /**
     * Get group information by ID
     */
    @GetMapping("/{groupId}")
    public ResponseEntity<?> getGroup(@PathVariable Long groupId) {
        try {
            Map<String, Object> group = groupRedisService.getGroup(groupId);
            if (group == null || group.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Group not found in cache");
            }
            return ResponseEntity.ok(group);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to retrieve group from cache: " + e.getMessage());
        }
    }

    /**
     * Get group information by code
     */
    @GetMapping("/code/{groupCode}")
    public ResponseEntity<?> getGroupByCode(@PathVariable String groupCode) {
        try {
            Map<String, Object> group = groupRedisService.getGroupByCode(groupCode);
            if (group == null || group.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Group not found with code: " + groupCode);
            }
            return ResponseEntity.ok(group);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to retrieve group from cache: " + e.getMessage());
        }
    }

    /**
     * Get all users in a group
     */
    @GetMapping("/{groupId}/users")
    public ResponseEntity<?> getGroupUsers(@PathVariable Long groupId) {
        try {
            Set<Object> users = userGroupRedisService.getGroupUsers(groupId);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to retrieve group users from cache: " + e.getMessage());
        }
    }

    /**
     * Get user-group relationship
     */
    @GetMapping("/relationship/{userId}/{groupId}")
    public ResponseEntity<?> getUserGroupRelationship(@PathVariable Long userId, @PathVariable Long groupId) {
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

    /**
     * Generate a random 6-character group code
     */
    private String generateGroupCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }
        return code.toString();
    }
}

