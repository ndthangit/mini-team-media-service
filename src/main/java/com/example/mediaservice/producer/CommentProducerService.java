package com.example.mediaservice.producer;

import com.example.mediaservice.entity.Comment;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class CommentProducerService {

    private final KafkaTemplate<String, Comment> commentTemplate;

    public void sendCommentCreated(Comment comment) {
        try {
            String key = String.valueOf(comment.getId());
            commentTemplate.send("comment-created", key, comment);
            log.info("Sent comment-created event with key: {} and comment: {}", key, comment);
        } catch (Exception e) {
            log.error("Error sending comment-created event: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to send comment-created event", e);
        }
    }

    public void sendCommentUpdated(Comment comment) {
        try {
            String key = String.valueOf(comment.getId());
            commentTemplate.send("comment-updated", key, comment);
            log.info("Sent comment-updated event with key: {} and comment: {}", key, comment);
        } catch (Exception e) {
            log.error("Error sending comment-updated event: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to send comment-updated event", e);
        }
    }

    public void sendCommentDeleted(Comment comment) {
        try {
            String key = String.valueOf(comment.getId());
            commentTemplate.send("comment-deleted", key, comment);
            log.info("Sent comment-deleted event with key: {} and comment: {}", key, comment);
        } catch (Exception e) {
            log.error("Error sending comment-deleted event: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to send comment-deleted event", e);
        }
    }
}

