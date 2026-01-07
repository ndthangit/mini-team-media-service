package com.example.mediaservice.service;

import com.example.mediaservice.dto.CommentDto;
import com.example.mediaservice.dto.UserDto;
import com.example.mediaservice.entity.Comment;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentRedisService {

    // Lưu comments cho bài post: post:{postId}:comments -> hash {commentId: commentJson}
    private static final String POST_COMMENTS_KEY_PREFIX = "post:";
    private static final String POST_COMMENTS_KEY_SUFFIX = ":comments";

    // Lưu replies cho comment: comment:{commentId}:replies -> hash {replyId: replyJson}
    private static final String COMMENT_REPLIES_KEY_PREFIX = "comment:";
    private static final String COMMENT_REPLIES_KEY_SUFFIX = ":replies";

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    private String getPostCommentsKey(String postId) {
        return POST_COMMENTS_KEY_PREFIX + postId + POST_COMMENTS_KEY_SUFFIX;
    }

    private String getCommentRepliesKey(String commentId) {
        return COMMENT_REPLIES_KEY_PREFIX + commentId + COMMENT_REPLIES_KEY_SUFFIX;
    }

    /**
     * Lưu comment vào Redis
     * - Nếu postId != null: lưu vào post:{postId}:comments
     * - Nếu postId == null và parentId != null: lưu vào comment:{parentId}:replies
     */
    public void saveComment(Comment comment) {
        try {
            UserDto authorDto = new UserDto(
                    String.valueOf(comment.getAuthor().getFirstName()),
                    String.valueOf(comment.getAuthor().getLastName()),
                    String.valueOf(comment.getAuthor().getEmail()),
                    String.valueOf(comment.getAuthor().getOccupation()),
                    String.valueOf(comment.getAuthor().getGender()),
                    String.valueOf(comment.getAuthor().getDateOfBirth()),
                    String.valueOf(comment.getAuthor().getAvatarUrl())
            );

            CommentDto commentDto = new CommentDto(
                    String.valueOf(comment.getId()),
                    comment.getPostId() != null ? String.valueOf(comment.getPostId()) : null,
                    comment.getParentId() != null ? String.valueOf(comment.getParentId()) : null,
                    authorDto,
                    String.valueOf(comment.getContent()),
                    comment.getCreatedAt()
            );

            String jsonValue = objectMapper.writeValueAsString(commentDto);
            String field = String.valueOf(comment.getId());

            // Comment cho bài post
            if (comment.getPostId() != null) {
                String key = getPostCommentsKey(String.valueOf(comment.getPostId()));
                redisTemplate.opsForHash().put(key, field, jsonValue);
                log.info("Saved comment {} for post {} in Redis", comment.getId(), comment.getPostId());
            }
            // Reply cho comment khác
            else if (comment.getParentId() != null) {
                String key = getCommentRepliesKey(String.valueOf(comment.getParentId()));
                redisTemplate.opsForHash().put(key, field, jsonValue);
                log.info("Saved reply {} for comment {} in Redis", comment.getId(), comment.getParentId());
            } else {
                log.warn("Comment {} has both postId and parentId null - cannot save", comment.getId());
                throw new IllegalArgumentException("Comment must have either postId or parentId");
            }
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize comment: {}", e.getMessage(), e);
            throw new RuntimeException("Error serializing comment", e);
        }
    }

    /**
     * Xóa comment từ Redis
     * - Nếu postId != null: xóa từ post:{postId}:comments
     * - Nếu parentId != null: xóa từ comment:{parentId}:replies
     */
    public void removeComment(Comment comment) {
        String commentId = String.valueOf(comment.getId());
        
        // Xóa comment của bài post
        if (comment.getPostId() != null) {
            String key = getPostCommentsKey(String.valueOf(comment.getPostId()));
            redisTemplate.opsForHash().delete(key, commentId);
            log.info("Removed comment {} from post {} in Redis", commentId, comment.getPostId());
        }
        // Xóa reply của comment
        else if (comment.getParentId() != null) {
            String key = getCommentRepliesKey(String.valueOf(comment.getParentId()));
            redisTemplate.opsForHash().delete(key, commentId);
            log.info("Removed reply {} from comment {} in Redis", commentId, comment.getParentId());
        }
    }

    /**
     * Lấy tất cả comments của một bài post
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
     * Lấy tất cả replies của một comment
     */
    public List<CommentDto> getRepliesByComment(String commentId) {
        String key = getCommentRepliesKey(commentId);
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);

        return entries.values().stream()
                .map(Object::toString)
                .map(this::deserializeComment)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Lấy một comment cụ thể theo ID từ bài post
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

    /**
     * Lấy một reply cụ thể theo ID từ comment cha
     */
    public CommentDto getReplyById(String parentCommentId, String replyId) {
        String key = getCommentRepliesKey(parentCommentId);
        Object value = redisTemplate.opsForHash().get(key, replyId);
        if (value == null) {
            log.warn("Reply {} not found in comment {}", replyId, parentCommentId);
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

