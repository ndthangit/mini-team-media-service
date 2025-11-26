package com.example.mediaservice.producer;

import com.example.mediaservice.entity.Group;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class GroupProducerService {

    private final KafkaTemplate<String, Group> groupTemplate;

    public void sendGroupCreated(Group group) {
        try {
            String key = String.valueOf(group.getId());
            groupTemplate.send("group-created", key, group);
            log.info("Sent group-created event with key: {} and group: {}", key, group);
        } catch (Exception e) {
            log.error("Error sending group-created event: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to send group-created event", e);
        }
    }

    public void sendGroupUpdated(Group group) {
        try {
            String key = String.valueOf(group.getId());
            groupTemplate.send("group-updated", key, group);
            log.info("Sent group-updated event with key: {} and group: {}", key, group);
        } catch (Exception e) {
            log.error("Error sending group-updated event: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to send group-updated event", e);
        }
    }
}


