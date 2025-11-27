package com.example.mediaservice.producer;

import com.example.mediaservice.entity.Channel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@AllArgsConstructor
@Service
public class ChannelProducerService {

    private final KafkaTemplate<String, Channel> channelTemplate;

    public void sendChannelCreated(Channel channel) {
        try {
            String key = String.valueOf(channel.getChannelId());
            channelTemplate.send("channel-created", key, channel);
            log.info("Sent channel-created event with key: {} and channel: {}", key, channel);
        } catch (Exception e) {
            log.error("Error sending channel-created event: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to send channel-created event", e);
        }
    }

    public void sendChannelUpdated(Channel channel) {
        try {
            String key = String.valueOf(channel.getChannelId());
            channelTemplate.send("channel-updated", key, channel);
            log.info("Sent channel-updated event with key: {} and channel: {}", key, channel);
        } catch (Exception e) {
            log.error("Error sending channel-updated event: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to send channel-updated event", e);
        }
    }
}
