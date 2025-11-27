package com.example.mediaservice.consumer;

import com.example.mediaservice.entity.relationship.UserChannel;
import com.example.mediaservice.service.UserChannelRedisService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class UserChannelConsumerService {

    private final UserChannelRedisService userChannelRedisService;

    @KafkaListener(topics = "user-channel-events", groupId = "media-service-user-channel-group")
    public void consumeUserChannelEvent(ConsumerRecord<String, UserChannel> record) {
        try {
            UserChannel userChannel = record.value();
            log.info("Received user-channel event with key: {} and userChannel: {}", record.key(), userChannel);

            // Save user-channel relationship to Redis
            userChannelRedisService.saveUserChannel(userChannel);

            log.info("Successfully processed user-channel event: userId={}, channelId={}",
                    userChannel.getUserId(), userChannel.getChannelId());
        } catch (Exception e) {
            log.error("Error processing user-channel event: {}", e.getMessage(), e);
        }
    }
}

