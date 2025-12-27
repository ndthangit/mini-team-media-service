package com.example.mediaservice.controller;

import com.example.mediaservice.dto.PostDto;
import com.example.mediaservice.entity.Post;
import com.example.mediaservice.entity.User;
import com.example.mediaservice.producer.PostProducerService;

import com.example.mediaservice.service.TokenService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@AllArgsConstructor
@RestController
@RequestMapping("/post-events")
@Slf4j
public class PostEventController {

    private final PostProducerService postProducerService;
    private final TokenService tokenService;

    /**
     * Create a new post
     */
    @PostMapping("/create")
    public ResponseEntity<String> createPost(@RequestBody PostDto postDto) {

        try {
            String postId = UUID.randomUUID().toString();


            User user = User.newBuilder()
                    .setFirstName(postDto.author().firstName())
                    .setLastName(postDto.author().lastName())
                    .setEmail(postDto.author().email())
                    .setOccupation(postDto.author().occupation())
                    .setGender(postDto.author().gender())
                    .setDateOfBirth(postDto.author().dateOfBirth())
                    .setAvatarUrl(postDto.author().avatarUrl())
                    .build();

            // Build Post entity
            Post post = Post.newBuilder()
                    .setId(postId)
                    .setGroupId(postDto.groupId())
                    .setChannelId(postDto.channelId())
                    .setAuthor(user)
                    .setContent(postDto.content())
                    .setLikes(0)
                    .build();

            // Send event to Kafka
            postProducerService.sendPostCreated(post);

            return ResponseEntity.accepted()
                    .body("Post creation request accepted. Post ID: " + postId);

//            log.info("Received post creation request:" + postDto);
//            return ResponseEntity.accepted()
//                    .body("Post creation request accepted.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to process post creation request: " + e.getMessage());
        }
    }

    /**
     * Update an existing post
     */
    @PutMapping("/update/{postId}")
    public ResponseEntity<String> updatePost(@PathVariable String postId, @RequestBody PostDto postDto) {
        try {
            // Build Post entity
            User user = User.newBuilder()
                    .setFirstName(postDto.author().firstName())
                    .setLastName(postDto.author().lastName())
                    .setEmail(postDto.author().email())
                    .setOccupation(postDto.author().occupation())
                    .setGender(postDto.author().gender())
                    .setDateOfBirth(postDto.author().dateOfBirth())
                    .setAvatarUrl(postDto.author().avatarUrl())
                    .build();

            Post post = Post.newBuilder()
                    .setId(postId)
                    .setGroupId(postDto.groupId())
                    .setChannelId(postDto.channelId())
                    .setAuthor(user)
                    .setContent(postDto.content())
                    .setLikes(postDto.likes())
                    .build();

            // Send event to Kafka
            postProducerService.sendPostUpdated(post);

            return ResponseEntity.accepted()
                    .body("Post update request accepted. Post ID: " + postId);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to process post update request: " + e.getMessage());
        }
    }

    /**
     * Delete a post
     */
    @DeleteMapping("/delete/{postId}")
    public ResponseEntity<String> deletePost(
            @PathVariable String postId,
            @RequestParam(required = false) String channelId) {
        try {
            // Build minimal Post entity for deletion
            Post post = Post.newBuilder()
                    .setId(postId)
                    .setChannelId(channelId)
                    .setAuthor(null)
                    .setContent("")
                    .setLikes(0)
                    .build();

            // Send event to Kafka
            postProducerService.sendPostDeleted(post);

            return ResponseEntity.accepted()
                    .body("Post deletion request accepted. Post ID: " + postId);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to process post deletion request: " + e.getMessage());
        }
    }


}