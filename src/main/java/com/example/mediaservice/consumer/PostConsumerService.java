package com.example.mediaservice.consumer;

import com.example.mediaservice.entity.Post;
import com.example.mediaservice.service.PostRedisService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@AllArgsConstructor
@Service
public class PostConsumerService {

    private final PostRedisService postRedisService;

    @KafkaListener(topics = "post-created", groupId = "${spring.application.name}")
    public void consumePostCreated(Post post) {
        try {
            log.info("Received post-created event: {}", post);

            // Add to group
            postRedisService.addPostToGroup(String.valueOf(post.getGroupId()), post);

            // Add to channel if exists
            if (post.getChannelId() != null) {
                postRedisService.addPostToChannel(String.valueOf(post.getChannelId()), post);
            }

            log.info("Successfully processed post-created event for post: {}", post.getId());
        } catch (Exception e) {
            log.error("Error processing post-created event: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "post-updated", groupId = "${spring.application.name}")
    public void consumePostUpdated(Post post) {
        try {
            log.info("Received post-updated event: {}", post);

            // Update in group
            postRedisService.addPostToGroup(String.valueOf(post.getGroupId()), post);

            // Update in channel if exists
            if (post.getChannelId() != null) {
                postRedisService.addPostToChannel(String.valueOf(post.getChannelId()), post);
            }

            log.info("Successfully processed post-updated event for post: {}", post.getId());
        } catch (Exception e) {
            log.error("Error processing post-updated event: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "post-deleted", groupId = "${spring.application.name}")
    public void consumePostDeleted(Post post) {
        try {
            log.info("Received post-deleted event: {}", post);

            // Remove from group
            postRedisService.removePostFromGroup(String.valueOf(post.getGroupId()), String.valueOf(post.getId()));

            // Remove from channel if exists
            if (post.getChannelId() != null) {
                postRedisService.removePostFromChannel(String.valueOf(post.getChannelId()), String.valueOf(post.getId()));
            }

            log.info("Successfully processed post-deleted event for post: {}", post.getId());
        } catch (Exception e) {
            log.error("Error processing post-deleted event: {}", e.getMessage(), e);
        }
    }
}