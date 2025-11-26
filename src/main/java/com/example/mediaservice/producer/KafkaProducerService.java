package com.example.mediaservice.producer;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerService {
    /**
     * Chúng ta dùng ApplicationRunner để chạy code này một lần
     * sau khi ứng dụng khởi động xong.
     */
    @Bean
    public ApplicationRunner runner(KafkaTemplate<String, String> kafkaTemplate) {
        return args -> {
            System.out.println(">>> Gửi tin nhắn test...");
            // use the args so static analysis does not flag it as unused
            System.out.println("Application started with " + (args != null ? args.getSourceArgs().length : 0) + " source args");
            kafkaTemplate.send("team-events", "Xin chào từ Spring Boot Kafka (đã tách file)!");
        };
    }
}
