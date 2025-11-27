package com.example.mediaservice.consumer;

import com.example.mediaservice.entity.relationship.UserGroup;
import com.example.mediaservice.service.UserGroupRedisService;
import lombok.AllArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserGroupConsumerService {

    private static final Logger logger = LoggerFactory.getLogger(UserGroupConsumerService.class);
    private final UserGroupRedisService userGroupRedisService;

    @KafkaListener(topics = "user-group-create", containerFactory = "userGroupKafkaListenerContainerFactory")
    public void consumeUserGroupCreate(ConsumerRecord<String, UserGroup> record) {
        String key = record.key();
        UserGroup userGroup = record.value();
        logger.info("Consumed record from topic='{}' key='{}' value='{}'", record.topic(), key, userGroup);

        try {
            userGroupRedisService.saveUserGroup(userGroup);
            logger.info("Successfully saved user-group relationship to Redis: User={}, Group={}, Type={}",
                    userGroup.getUserId(), userGroup.getGroupId(), userGroup.getUserGroupRelationship());
        } catch (Exception e) {
            logger.error("Failed to save user-group relationship to Redis: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "user-group-join", containerFactory = "userGroupKafkaListenerContainerFactory")
    public void consumeUserGroupJoin(ConsumerRecord<String, UserGroup> record) {
        String key = record.key();
        UserGroup userGroup = record.value();
        logger.info("Consumed record from topic='{}' key='{}' value='{}'", record.topic(), key, userGroup);

        try {
            userGroupRedisService.saveUserGroup(userGroup);
            logger.info("Successfully saved user-group join relationship to Redis: User={}, Group={}",
                    userGroup.getUserId(), userGroup.getGroupId());
        } catch (Exception e) {
            logger.error("Failed to save user-group join relationship to Redis: {}", e.getMessage(), e);
        }
    }
}

