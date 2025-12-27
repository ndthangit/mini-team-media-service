package com.example.mediaservice.dto;

public record PostDto(
        String id,
        String groupId,
        String channelId,
        UserDto author,
        String content,
        int likes
) {}