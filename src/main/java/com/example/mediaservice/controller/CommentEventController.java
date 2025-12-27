package com.example.mediaservice.controller;

import com.example.mediaservice.dto.CommentDto;
import com.example.mediaservice.entity.Comment;
import com.example.mediaservice.producer.CommentProducerService;
import com.example.mediaservice.service.CommentRedisService;
import com.example.mediaservice.service.TokenService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/comment-events")
@AllArgsConstructor
public class CommentEventController {

    private final CommentProducerService commentProducerService;
    private final CommentRedisService commentRedisService;
    private final TokenService tokenService;

    /**
     * Create a new comment
     */
    @PostMapping("/create")
    public ResponseEntity<String> createComment(@RequestBody CommentDto commentDto) {
        try {
            String commentId = UUID.randomUUID().toString();

            // Build Comment entity
            Comment comment = Comment.newBuilder()
                    .setId(commentId)
                    .setPostId(commentDto.postId())
                    .setAuthor(commentDto.author())
                    .setContent(commentDto.content())
                    .build();

            // Send event to Kafka
            commentProducerService.sendCommentCreated(comment);

            return ResponseEntity.accepted()
                    .body("Comment creation request accepted. Comment ID: " + commentId);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to process comment creation request: " + e.getMessage());
        }
    }

    /**
     * Update an existing comment
     */
    @PutMapping("/update/{commentId}")
    public ResponseEntity<String> updateComment(@PathVariable String commentId, @RequestBody CommentDto commentDto) {
        try {
            // Build Comment entity
            Comment comment = Comment.newBuilder()
                    .setId(commentId)
                    .setPostId(commentDto.postId())
                    .setAuthor(commentDto.author())
                    .setContent(commentDto.content())
                    .build();

            // Send event to Kafka
            commentProducerService.sendCommentUpdated(comment);

            return ResponseEntity.accepted()
                    .body("Comment update request accepted. Comment ID: " + commentId);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to process comment update request: " + e.getMessage());
        }
    }

    /**
     * Delete a comment
     */
    @DeleteMapping("/delete/{commentId}")
    public ResponseEntity<String> deleteComment(
            @PathVariable String commentId,
            @RequestParam String postId) {
        try {
            // Build minimal Comment entity for deletion
            Comment comment = Comment.newBuilder()
                    .setId(commentId)
                    .setPostId(postId)
                    .setAuthor(null)
                    .setContent("")
                    .build();

            // Send event to Kafka
            commentProducerService.sendCommentDeleted(comment);

            return ResponseEntity.accepted()
                    .body("Comment deletion request accepted. Comment ID: " + commentId);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to process comment deletion request: " + e.getMessage());
        }
    }

    /**
     * Get all comments for a post
     */
    @GetMapping("/post/{postId}")
    public ResponseEntity<?> getCommentsByPost(@PathVariable String postId) {
        try {
            List<CommentDto> comments = commentRedisService.getCommentsByPost(postId);
            return ResponseEntity.ok(comments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to retrieve comments: " + e.getMessage());
        }
    }

    /**
     * Get a specific comment by ID
     */
    @GetMapping("/{postId}/{commentId}")
    public ResponseEntity<?> getCommentById(@PathVariable String postId, @PathVariable String commentId) {
        try {
            CommentDto comment = commentRedisService.getCommentById(postId, commentId);
            if (comment == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(comment);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to retrieve comment: " + e.getMessage());
        }
    }
}

