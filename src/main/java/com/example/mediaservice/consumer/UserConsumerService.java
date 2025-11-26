package com.example.mediaservice.consumer;

import com.example.mediaservice.entity.User;
import com.example.mediaservice.service.UserRedisService;
import lombok.AllArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserConsumerService {

    private static final Logger logger = LoggerFactory.getLogger(UserConsumerService.class);
    private final UserRedisService userRedisService;


    @KafkaListener(topics = "user-update", containerFactory = "kafkaListenerContainerFactory")
    public void read(ConsumerRecord<String, User> record){
        String key = record.key();
        User user = record.value();
        logger.info("Consumed record from topic='{}' key='{}' value='{}'", record.topic(), key, user);

        // Save user to Redis
        try {
            userRedisService.saveUser(user);
            logger.info("Successfully saved user to Redis: {}", user.getEmail());
        } catch (Exception e) {
            logger.error("Failed to save user to Redis: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "user-create", containerFactory = "kafkaListenerContainerFactory")
    public void readCreate(ConsumerRecord<String, User> record) {
        String key = record.key();
        User user = record.value();
        logger.info("Consumed record from topic='{}' key='{}' value='{}'", record.topic(), key, user);

        // Save user to Redis
        try {
            userRedisService.saveUser(user);
            logger.info("Successfully saved user to Redis: {}", user.getEmail());
        } catch (Exception e) {
            logger.error("Failed to save user to Redis: {}", e.getMessage(), e);
        }
    }


}
