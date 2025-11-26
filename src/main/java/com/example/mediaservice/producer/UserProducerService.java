package com.example.mediaservice.producer;

import com.example.mediaservice.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;


@Service
@Slf4j
public class UserProducerService {


    @Autowired
    private  KafkaTemplate<String, User> userTemplate;

//    @Autowired
//    private  KafkaTemplate<String, String> userTemplate;

    public void send(User user){

        String key = String.valueOf(user.getEmail());
        CompletableFuture<SendResult<String, User>> future = userTemplate.send("user-update", key, user );

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Sent update key= {} user='{}' with offset=[{}]", user.getEmail(), user, result.getRecordMetadata().offset());
            } else {
                log.error("Unable to send user='{}'", user, ex);
            }
        });
    }

    public void create(User user){

        String key = String.valueOf(user.getEmail());
        CompletableFuture<SendResult<String, User>> future = userTemplate.send("user-create", key, user );

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Sent create key= {} user='{}' with offset=[{}]", user.getEmail(), user, result.getRecordMetadata().offset());
            } else {
                log.error("Unable to send user='{}'", user, ex);
            }
        });
    }
    
}
