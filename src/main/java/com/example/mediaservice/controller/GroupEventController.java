package com.example.mediaservice.controller;

import com.example.mediaservice.entity.Channel;
import com.example.mediaservice.entity.Group;
import com.example.mediaservice.entity.relationship.UserChannel;
import com.example.mediaservice.entity.relationship.UserGroup;
import com.example.mediaservice.producer.ChannelProducerService;
import com.example.mediaservice.producer.GroupProducerService;
import com.example.mediaservice.producer.UserChannelProducerService;
import com.example.mediaservice.producer.UserGroupProducerService;
import com.example.mediaservice.service.TokenService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/group-events")
@AllArgsConstructor
public class GroupEventController {
    private final GroupProducerService groupProducerService;
    private final ChannelProducerService channelProducerService;
    private final UserGroupProducerService userGroupProducerService;
    private final TokenService tokenService;
    private final UserChannelProducerService userChannelProducerService;

    @PostMapping("/create/{groupName}")
    public ResponseEntity<String> createGroup(@PathVariable String groupName) {
        String groupId = null;
        try {
            // Generate unique group ID
            groupId = UUID.randomUUID().toString();
            String userEmail = tokenService.getEmailFromToken();

            // Create Group event
            Group group = Group.newBuilder()
                    .setId(groupId)
                    .setName(groupName)
                    .setOwner(userEmail)
                    .build();
            groupProducerService.sendGroupCreated(group);


            String channelId = "ch_" + UUID.randomUUID().toString();
            Channel generalChannel = Channel.newBuilder()
                    .setChannelId(channelId)
                    .setName("general")
                    .setGroupId(groupId)
                    .build();
            channelProducerService.sendChannelCreated(generalChannel);


            // Create UserGroup relationship event
            UserGroup userGroup = UserGroup.newBuilder()
                    .setUserId(userEmail)
                    .setGroupId(groupId)
                    .setUserGroupRelationship(com.example.mediaservice.entity.relationship.UserGroupRelationship.CREATE)
                    .build();

            UserChannel userChannel = UserChannel.newBuilder()
                    .setUserId(userEmail) // Sử dụng email từ token thay vì request
                    .setChannelId(channelId)
                    .setUserChannelRelationship(com.example.mediaservice.entity.relationship.UserChannelRelationship.CREATE)
                    .build();

            CompletableFuture<Void> userGroupFuture = CompletableFuture.runAsync(() ->
                    userGroupProducerService.sendUserGroupEvent(userGroup)
            );

            CompletableFuture<Void> userChannelFuture = CompletableFuture.runAsync(() ->
                    userChannelProducerService.sendUserChannelEvent(userChannel)
            );

            // Chờ cả hai hoàn thành
            CompletableFuture.allOf(userGroupFuture, userChannelFuture)
                    .get(30, TimeUnit.SECONDS);

            return ResponseEntity.accepted().body("Group creation request accepted. Group ID: " + groupId + ", Default channel 'general' created");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to process group creation request: " + e.getMessage());
        }
    }
}
