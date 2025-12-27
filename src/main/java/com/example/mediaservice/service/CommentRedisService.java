package com.example.mediaservice.service;

import com.example.mediaservice.dto.CommentDto;
import com.example.mediaservice.entity.Comment;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentRedisService {

    private static final String POST_COMMENTS_KEY_PREFIX = "post:";
    private static final String POST_COMMENTS_KEY_SUFFIX = ":comments";

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    private String getPostCommentsKey(String postId) {
        return POST_COMMENTS_KEY_PREFIX + postId + POST_COMMENTS_KEY_SUFFIX;
    }

    /**
     * Adds or updates a comment for a specific post
     */
    public void addCommentToPost(String postId, Comment comment) {
        String key = getPostCommentsKey(postId);
        String field = String.valueOf(comment.getId());
        try {
            CommentDto commentDto = new CommentDto(
                    String.valueOf(comment.getId()),
                    String.valueOf(comment.getPostId()),
                    comment.getAuthor(),
                    String.valueOf(comment.getContent())
            );

            String jsonValue = objectMapper.writeValueAsString(commentDto);
            redisTemplate.opsForHash().put(key, field, jsonValue);
            log.info("Added comment {} to post {} in Redis", comment.getId(), postId);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize comment: {}", e.getMessage(), e);
            throw new RuntimeException("Error serializing comment", e);
        }
    }

    /**
     * Removes a comment from a post
     */
    public void removeCommentFromPost(String postId, String commentId) {
        String key = getPostCommentsKey(postId);
        redisTemplate.opsForHash().delete(key, commentId);
        log.info("Removed comment {} from post {} in Redis", commentId, postId);
    }

    /**
     * Retrieves all comments for a specific post
     */
    public List<CommentDto> getCommentsByPost(String postId) {
        String key = getPostCommentsKey(postId);
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);

        return entries.values().stream()
                .map(Object::toString)
                .map(this::deserializeComment)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a specific comment by ID from a post
     */
    public CommentDto getCommentById(String postId, String commentId) {
        String key = getPostCommentsKey(postId);
        Object value = redisTemplate.opsForHash().get(key, commentId);
        if (value == null) {
            log.warn("Comment {} not found in post {}", commentId, postId);
            return null;
        }
        return deserializeComment(value.toString());
    }

    private CommentDto deserializeComment(String json) {
        try {
            return objectMapper.readValue(json, CommentDto.class);
        } catch (Exception e) {
            log.error("Failed to deserialize comment: {}", e.getMessage(), e);
            return null;
        }
    }
}

