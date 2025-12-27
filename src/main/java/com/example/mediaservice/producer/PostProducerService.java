package com.example.mediaservice.producer;

import com.example.mediaservice.entity.Post;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@AllArgsConstructor
@Service
public class PostProducerService {

    private final KafkaTemplate<String, Post> postTemplate;

    public void sendPostCreated(Post post) {
        try {
            String key = String.valueOf(post.getId());
            postTemplate.send("post-created", key, post);
            log.info("Sent post-created event with key: {} and post: {}", key, post);
        } catch (Exception e) {
            log.error("Error sending post-created event: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to send post-created event", e);
        }
    }

    public void sendPostUpdated(Post post) {
        try {
            String key = String.valueOf(post.getId());
            postTemplate.send("post-updated", key, post);
            log.info("Sent post-updated event with key: {} and post: {}", key, post);
        } catch (Exception e) {
            log.error("Error sending post-updated event: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to send post-updated event", e);
        }
    }

    public void sendPostDeleted(Post post) {
        try {
            String key = String.valueOf(post.getId());
            postTemplate.send("post-deleted", key, post);
            log.info("Sent post-deleted event with key: {} and post: {}", key, post);
        } catch (Exception e) {
            log.error("Error sending post-deleted event: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to send post-deleted event", e);
        }
    }
}