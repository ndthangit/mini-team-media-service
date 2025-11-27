package com.example.mediaservice.consumer;

import com.example.mediaservice.entity.Channel;
import com.example.mediaservice.service.ChannelRedisService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@AllArgsConstructor
@Service
public class ChannelConsumerService {

    private final ChannelRedisService channelRedisService;

    @KafkaListener(topics = "channel-created", groupId = "media-service-channel-group")
    public void consumeChannelCreated(ConsumerRecord<String, Channel> record) {
        try {
            Channel channel = record.value();
            log.info("Received channel-created event with key: {} and channel: {}", record.key(), channel);

            // Save channel to Redis
            channelRedisService.saveChannel(channel);

            log.info("Successfully processed channel-created event for channel ID: {}", channel.getChannelId());
        } catch (Exception e) {
            log.error("Error processing channel-created event: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "channel-updated", groupId = "media-service-channel-group")
    public void consumeChannelUpdated(ConsumerRecord<String, Channel> record) {
        try {
            Channel channel = record.value();
            log.info("Received channel-updated event with key: {} and channel: {}", record.key(), channel);

            // Update channel in Redis
            channelRedisService.saveChannel(channel);

            log.info("Successfully processed channel-updated event for channel ID: {}", channel.getChannelId());
        } catch (Exception e) {
            log.error("Error processing channel-updated event: {}", e.getMessage(), e);
        }
    }
}
