package com.example.mediaservice.controller;

import com.example.mediaservice.dto.PostDto;
import com.example.mediaservice.service.PostRedisService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/post")
class PostController {

    private final PostRedisService postRedisService;

    /**
     * Get all posts from a group
     */
    @GetMapping("/group/{groupId}")
    public ResponseEntity<?> getPostsByGroup(@PathVariable String groupId) {
        try {
            List<PostDto> posts = postRedisService.getPostsByGroup(groupId);
            return ResponseEntity.ok(posts);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to retrieve posts: " + e.getMessage());
        }
    }

    /**
     * Get all posts from a channel
     */
    @GetMapping("/channel/{channelId}")
    public ResponseEntity<?> getPostsByChannel(@PathVariable String channelId) {
        try {
            List<PostDto> posts = postRedisService.getPostsByChannel(channelId);
            return ResponseEntity.ok(posts);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to retrieve posts: " + e.getMessage());
        }
    }

    /**
     * Get a specific post by ID
     */
    @GetMapping("/{groupId}/{postId}")
    public ResponseEntity<?> getPostById(@PathVariable String groupId, @PathVariable String postId) {
        try {
            PostDto post = postRedisService.getPostById(groupId, postId);
            if (post == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(post);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to retrieve post: " + e.getMessage());
        }
    }
}
