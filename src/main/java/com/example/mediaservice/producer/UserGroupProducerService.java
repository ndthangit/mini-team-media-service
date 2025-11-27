package com.example.mediaservice.producer;

import com.example.mediaservice.entity.relationship.UserGroup;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class UserGroupProducerService {

    private final KafkaTemplate<String, UserGroup> userGroupTemplate;

    public void sendUserGroupEvent(UserGroup userGroup) {
        try {
            String key = userGroup.getUserId() + ":" + userGroup.getGroupId();
            String topic = userGroup.getUserGroupRelationship().toString().equals("CREATE")
                    ? "user-group-create"
                    : "user-group-join";

            userGroupTemplate.send(topic, key, userGroup);
            log.info("Sent {} event with key: {} and userGroup: {}", topic, key, userGroup);
        } catch (Exception e) {
            log.error("Error sending user-group event: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to send user-group event", e);
        }
    }
}

