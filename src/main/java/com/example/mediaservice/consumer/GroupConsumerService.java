package com.example.mediaservice.consumer;

import com.example.mediaservice.entity.Group;
import com.example.mediaservice.service.GroupRedisService;
import lombok.AllArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class GroupConsumerService {

    private static final Logger logger = LoggerFactory.getLogger(GroupConsumerService.class);
    private final GroupRedisService groupRedisService;

    @KafkaListener(topics = "group-created", containerFactory = "groupKafkaListenerContainerFactory")
    public void consumeGroupCreated(ConsumerRecord<String, Group> record) {
        String key = record.key();
        Group group = record.value();
        logger.info("Consumed record from topic='{}' key='{}' value='{}'", record.topic(), key, group);

        try {
            groupRedisService.saveGroup(group);
            logger.info("Successfully saved group to Redis: {}", group.getId());
        } catch (Exception e) {
            logger.error("Failed to save group to Redis: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "group-updated", containerFactory = "groupKafkaListenerContainerFactory")
    public void consumeGroupUpdated(ConsumerRecord<String, Group> record) {
        String key = record.key();
        Group group = record.value();
        logger.info("Consumed record from topic='{}' key='{}' value='{}'", record.topic(), key, group);

        try {
            groupRedisService.saveGroup(group);
            logger.info("Successfully updated group in Redis: {}", group.getId());
        } catch (Exception e) {
            logger.error("Failed to update group in Redis: {}", e.getMessage(), e);
        }
    }
}


