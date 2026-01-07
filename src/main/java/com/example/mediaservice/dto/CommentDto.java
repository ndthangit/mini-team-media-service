package com.example.mediaservice.dto;
public record CommentDto(
        String id,
        String postId,
        String parentId,
        UserDto author,
        String content,
        Long createdAt
) {
    /**
     * Kiểm tra xem comment này có phải là reply cho comment khác không
     * @return true nếu postId null và parentId không null
     */
    public boolean isReplyToComment() {
        return postId == null && parentId != null;
    }

    /**
     * Kiểm tra xem comment này có phải là comment cho bài post không
     * @return true nếu postId không null
     */
    public boolean isCommentForPost() {
        return postId != null;
    }
}

