package com.example.mediaservice.controller;

import com.example.mediaservice.dto.CommentDto;
import com.example.mediaservice.service.CommentRedisService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/comment")
public class CommentController {

    private final CommentRedisService commentRedisService;

    /**
     * Lấy tất cả comments của một bài post
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
     * Lấy tất cả replies của một comment
     */
    @GetMapping("/replies/{commentId}")
    public ResponseEntity<?> getRepliesByComment(@PathVariable String commentId) {
        try {
            List<CommentDto> replies = commentRedisService.getRepliesByComment(commentId);
            return ResponseEntity.ok(replies);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to retrieve replies: " + e.getMessage());
        }
    }

    /**
     * Lấy một comment cụ thể theo ID từ bài post
     */
    @GetMapping("/post/{postId}/{commentId}")
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

    /**
     * Lấy một reply cụ thể theo ID từ comment cha
     */
    @GetMapping("/reply/{parentCommentId}/{replyId}")
    public ResponseEntity<?> getReplyById(@PathVariable String parentCommentId, @PathVariable String replyId) {
        try {
            CommentDto reply = commentRedisService.getReplyById(parentCommentId, replyId);
            if (reply == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(reply);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to retrieve reply: " + e.getMessage());
        }
    }
}
