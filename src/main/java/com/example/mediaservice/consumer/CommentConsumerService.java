package com.example.mediaservice.consumer;

import com.example.mediaservice.entity.Comment;
import com.example.mediaservice.service.CommentRedisService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class CommentConsumerService {

    private final CommentRedisService commentRedisService;

    @KafkaListener(topics = "comment-created", groupId = "${spring.application.name}")
    public void consumeCommentCreated(Comment comment) {
        try {
            log.info("Received comment-created event: {}", comment);
            commentRedisService.addCommentToPost(String.valueOf(comment.getPostId()), comment);
            log.info("Successfully processed comment-created event for comment: {}", comment.getId());
        } catch (Exception e) {
            log.error("Error processing comment-created event: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "comment-updated", groupId = "${spring.application.name}")
    public void consumeCommentUpdated(Comment comment) {
        try {
            log.info("Received comment-updated event: {}", comment);
            commentRedisService.addCommentToPost(String.valueOf(comment.getPostId()), comment);
            log.info("Successfully processed comment-updated event for comment: {}", comment.getId());
        } catch (Exception e) {
            log.error("Error processing comment-updated event: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "comment-deleted", groupId = "${spring.application.name}")
    public void consumeCommentDeleted(Comment comment) {
        try {
            log.info("Received comment-deleted event: {}", comment);
            commentRedisService.removeCommentFromPost(String.valueOf(comment.getPostId()), String.valueOf(comment.getId()));
            log.info("Successfully processed comment-deleted event for comment: {}", comment.getId());
        } catch (Exception e) {
            log.error("Error processing comment-deleted event: {}", e.getMessage(), e);
        }
    }
}

