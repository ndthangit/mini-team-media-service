package com.example.mediaservice.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumerService.class);

    @KafkaListener(id = "myId", topics = "team-events")
    public void listen(String message) {
        System.out.println("<<< Nhận được tin nhắn: " + message);
    }

//    @KafkaListener(id = "update-user", topics = "neo4j-users")
//    public void listenNeo4j(GenericRecord record) { // <-- LỖI LÀ Ở ĐÂY, ĐÃ SỬA TỪ (User message)
//        System.out.println("<<< Nhận được tin nhắn thô (raw): " + record.toString());
//
//        // Bây giờ, chúng ta tự chuyển đổi nó sang đối tượng User
//        try {
//            User user = new User();
//
//
//
//            // Lấy trường 'email'
//            if (record.get("email") != null) {
//                GenericRecord emailStruct = (GenericRecord) record.get("email");
//                // Lấy giá trị từ trường "S" (String)
//                user.setEmail(emailStruct.get("S").toString());
//            }
//
//
//
//            // ... làm tương tự cho các trường khác ...
//
//            System.out.println(">>> Đã chuyển đổi thành công: " + user);
//
//            // ... (Code logic nghiệp vụ của bạn ở đây) ...
//
//        } catch (Exception e) {
//            System.err.println("Lỗi khi tự parse Avro record: " + e.getMessage());
//            // Xử lý lỗi
//        }
//    }
}