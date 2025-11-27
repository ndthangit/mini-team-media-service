package com.example.mediaservice.producer;

import com.example.mediaservice.entity.relationship.UserChannel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class UserChannelProducerService {

    private final KafkaTemplate<String, UserChannel> userChannelTemplate;

    public void sendUserChannelEvent(UserChannel userChannel) {
        try {
            String key = userChannel.getUserId() + ":" + userChannel.getChannelId();
            userChannelTemplate.send("user-channel-events", key, userChannel);
            log.info("Sent user-channel event with key: {} and userChannel: {}", key, userChannel);
        } catch (Exception e) {
            log.error("Error sending user-channel event: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to send user-channel event", e);
        }
    }
}

