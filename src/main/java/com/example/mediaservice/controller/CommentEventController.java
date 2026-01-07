package com.example.mediaservice.controller;

import com.example.mediaservice.dto.CommentDto;
import com.example.mediaservice.entity.Comment;
import com.example.mediaservice.entity.CommentEventType;
import com.example.mediaservice.entity.User;
import com.example.mediaservice.producer.CommentProducerService;
import com.example.mediaservice.service.CommentRedisService;
import com.example.mediaservice.service.TokenService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/comment-events")
@AllArgsConstructor
@Slf4j
public class CommentEventController {

    private final CommentProducerService commentProducerService;
    private final CommentRedisService commentRedisService;
    private final TokenService tokenService;

    /**
     * Tạo comment mới cho bài post
     */
    @PostMapping("/create/comment")
    public ResponseEntity<String> createCommentForPost(@RequestBody CommentDto commentDto) {
        try {
            if (commentDto.postId() == null) {
                return ResponseEntity.badRequest()
                        .body("postId is required for post comment");
            }

            String commentId = UUID.randomUUID().toString();
            long timestamp = System.currentTimeMillis();

            User user = User.newBuilder()
                    .setFirstName(commentDto.author().firstName())
                    .setLastName(commentDto.author().lastName())
                    .setEmail(commentDto.author().email())
                    .setOccupation(commentDto.author().occupation())
                    .setGender(commentDto.author().gender())
                    .setDateOfBirth(commentDto.author().dateOfBirth())
                    .setAvatarUrl(commentDto.author().avatarUrl())
                    .build();

            // Build Comment entity cho bài post
            Comment comment = Comment.newBuilder()
                    .setId(commentId)
                    .setEventType(CommentEventType.CREATED)
                    .setPostId(commentDto.postId())
                    .setParentId(null)
                    .setAuthor(user)
                    .setContent(commentDto.content())
                    .setCreatedAt(timestamp)
                    .build();
            log.info("Received comment creation request:" + comment);

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
     * Tạo reply cho một comment khác
     */
    @PostMapping("/create/reply")
    public ResponseEntity<String> createReplyForComment(@RequestBody CommentDto commentDto) {
        try {
            if (commentDto.parentId() == null) {
                return ResponseEntity.badRequest()
                        .body("parentId is required for reply comment");
            }

            String commentId = UUID.randomUUID().toString();
            long timestamp = System.currentTimeMillis();

            User user = User.newBuilder()
                    .setFirstName(commentDto.author().firstName())
                    .setLastName(commentDto.author().lastName())
                    .setEmail(commentDto.author().email())
                    .setOccupation(commentDto.author().occupation())
                    .setGender(commentDto.author().gender())
                    .setDateOfBirth(commentDto.author().dateOfBirth())
                    .setAvatarUrl(commentDto.author().avatarUrl())
                    .build();

            // Build Comment entity cho reply
            Comment comment = Comment.newBuilder()
                    .setId(commentId)
                    .setPostId(null) // postId null cho biết đây là reply
                    .setParentId(commentDto.parentId())
                    .setEventType(CommentEventType.CREATED)
                    .setAuthor(user)
                    .setContent(commentDto.content())
                    .setCreatedAt(timestamp)
                    .build();

            // Send event to Kafka
            commentProducerService.sendCommentCreated(comment);

            return ResponseEntity.accepted()
                    .body("Reply creation request accepted. Reply ID: " + commentId);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to process reply creation request: " + e.getMessage());
        }
    }

    /**
     * Cập nhật comment hoặc reply
     */
    @PutMapping("/update/{commentId}")
    public ResponseEntity<String> updateComment(@PathVariable String commentId, @RequestBody CommentDto commentDto) {
        try {
            long timestamp = System.currentTimeMillis();

            User user = User.newBuilder()
                    .setFirstName(commentDto.author().firstName())
                    .setLastName(commentDto.author().lastName())
                    .setEmail(commentDto.author().email())
                    .setOccupation(commentDto.author().occupation())
                    .setGender(commentDto.author().gender())
                    .setDateOfBirth(commentDto.author().dateOfBirth())
                    .setAvatarUrl(commentDto.author().avatarUrl())
                    .build();

            // Build Comment entity
            Comment comment = Comment.newBuilder()
                    .setId(commentId)
                    .setPostId(commentDto.postId()) // có thể null nếu là reply
                    .setParentId(commentDto.parentId())
                    .setAuthor(user)
                    .setContent(commentDto.content())
                    .setCreatedAt(timestamp)
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
     * Xóa comment hoặc reply
     * Cần truyền postId hoặc parentId để xác định loại comment
     */
    @DeleteMapping("/delete/{commentId}")
    public ResponseEntity<String> deleteComment(
            @PathVariable String commentId,
            @RequestParam(required = false) String postId,
            @RequestParam(required = false) String parentId) {
        try {
            if (postId == null && parentId == null) {
                return ResponseEntity.badRequest()
                        .body("Either postId or parentId must be provided");
            }

            // Build minimal Comment entity for deletion
            Comment comment = Comment.newBuilder()
                    .setId(commentId)
                    .setPostId(postId)
                    .setParentId(parentId)
                    .setAuthor(null)
                    .setContent("")
                    .setCreatedAt(0L)
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
    @GetMapping("/comment/{commentId}/replies")
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
     * Lấy một comment cụ thể từ post
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
     * Lấy một reply cụ thể từ comment
     */
    @GetMapping("/comment/{commentId}/reply/{replyId}")
    public ResponseEntity<?> getReplyById(@PathVariable String commentId, @PathVariable String replyId) {
        try {
            CommentDto reply = commentRedisService.getReplyById(commentId, replyId);
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

