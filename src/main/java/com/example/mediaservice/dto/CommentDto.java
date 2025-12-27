package com.example.mediaservice.dto;

import com.example.mediaservice.entity.User;

public record CommentDto(
        String id,
        String postId,
        User author,
        String content
) {
}

