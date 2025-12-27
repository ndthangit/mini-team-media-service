package com.example.mediaservice.service;

import com.example.mediaservice.dto.PostDto;
import com.example.mediaservice.dto.UserDto;
import com.example.mediaservice.entity.Post;
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

@Slf4j
@RequiredArgsConstructor
@Service
public class PostRedisService {

    private static final String GROUP_POSTS_KEY_PREFIX = "group:";
    private static final String GROUP_POSTS_KEY_SUFFIX = ":posts";
    private static final String CHANNEL_POSTS_KEY_PREFIX = "channel:";
    private static final String CHANNEL_POSTS_KEY_SUFFIX = ":posts";

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    private String getGroupPostsKey(String groupId) {
        return GROUP_POSTS_KEY_PREFIX + groupId + GROUP_POSTS_KEY_SUFFIX;
    }

    private String getChannelPostsKey(String channelId) {
        return CHANNEL_POSTS_KEY_PREFIX + channelId + CHANNEL_POSTS_KEY_SUFFIX;
    }

    /**
     * Adds or updates a post in a group
     */
    public void addPostToGroup(String groupId, Post post) {
        String key = getGroupPostsKey(groupId);
        String field = String.valueOf(post.getId());

        try {

            UserDto userDto = new UserDto(
                    String.valueOf(post.getAuthor().getEmail()),
                    String.valueOf(post.getAuthor().getFirstName()),
                    String.valueOf(post.getAuthor().getLastName()),
                    post.getAuthor().getDateOfBirth() != null ? String.valueOf(post.getAuthor().getDateOfBirth()) : null,
                    String.valueOf(post.getAuthor().getGender()),
                    post.getAuthor().getAvatarUrl() != null ? String.valueOf(post.getAuthor().getAvatarUrl()) : null,
                    String.valueOf(post.getAuthor().getOccupation())
            );
            PostDto postDto = new PostDto(
                    String.valueOf(post.getId()),
                    String.valueOf(post.getGroupId()),
                    post.getChannelId() != null ? String.valueOf(post.getChannelId()) : null,
                    userDto,
                    String.valueOf(post.getContent()),
                    post.getLikes()
            );

            String jsonValue = objectMapper.writeValueAsString(postDto);
            redisTemplate.opsForHash().put(key, field, jsonValue);
            log.info("Added post {} to group {} in Redis", post.getId(), groupId);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize post: {}", e.getMessage(), e);
            throw new RuntimeException("Error serializing post", e);
        }
    }

    /**
     * Adds or updates a post in a channel
     */
    public void addPostToChannel(String channelId, Post post) {
        if (channelId == null) {
            return;
        }

        String key = getChannelPostsKey(channelId);
        String field = String.valueOf(post.getId());

        try {
            UserDto userDto = new UserDto(
                    String.valueOf(post.getAuthor().getEmail()),
                    String.valueOf(post.getAuthor().getFirstName()),
                    String.valueOf(post.getAuthor().getLastName()),
                    post.getAuthor().getDateOfBirth() != null ? String.valueOf(post.getAuthor().getDateOfBirth()) : null,
                    String.valueOf(post.getAuthor().getGender()),
                    post.getAuthor().getAvatarUrl() != null ? String.valueOf(post.getAuthor().getAvatarUrl()) : null,
                    String.valueOf(post.getAuthor().getOccupation())
            );
            PostDto postDto = new PostDto(
                    String.valueOf(post.getId()),
                    String.valueOf(post.getGroupId()),
                    String.valueOf(post.getChannelId()),
                    userDto,
                    String.valueOf(post.getContent()),
                    post.getLikes()

            );

            String jsonValue = objectMapper.writeValueAsString(postDto);
            redisTemplate.opsForHash().put(key, field, jsonValue);
            log.info("Added post {} to channel {} in Redis", post.getId(), channelId);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize post: {}", e.getMessage(), e);
            throw new RuntimeException("Error serializing post", e);
        }
    }

    /**
     * Removes a post from a group
     */
    public void removePostFromGroup(String groupId, String postId) {
        String key = getGroupPostsKey(groupId);
        redisTemplate.opsForHash().delete(key, postId);
        log.info("Removed post {} from group {} in Redis", postId, groupId);
    }

    /**
     * Removes a post from a channel
     */
    public void removePostFromChannel(String channelId, String postId) {
        if (channelId == null) {
            return;
        }

        String key = getChannelPostsKey(channelId);
        redisTemplate.opsForHash().delete(key, postId);
        log.info("Removed post {} from channel {} in Redis", postId, channelId);
    }

    /**
     * Retrieves all posts for a specific group
     */
    public List<PostDto> getPostsByGroup(String groupId) {
        String key = getGroupPostsKey(groupId);
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);

        return entries.values().stream()
                .map(Object::toString)
                .map(this::deserializePost)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all posts for a specific channel
     */
    public List<PostDto> getPostsByChannel(String channelId) {
        String key = getChannelPostsKey(channelId);
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);

        return entries.values().stream()
                .map(Object::toString)
                .map(this::deserializePost)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a specific post by ID from group
     */
    public PostDto getPostById(String groupId, String postId) {
        String key = getGroupPostsKey(groupId);
        Object value = redisTemplate.opsForHash().get(key, postId);

        if (value == null) {
            log.warn("Post {} not found in group {}", postId, groupId);
            return null;
        }

        return deserializePost(value.toString());
    }

    private PostDto deserializePost(String json) {
        try {
            return objectMapper.readValue(json, PostDto.class);
        } catch (Exception e) {
            log.error("Failed to deserialize post: {}", e.getMessage(), e);
            return null;
        }
    }
}