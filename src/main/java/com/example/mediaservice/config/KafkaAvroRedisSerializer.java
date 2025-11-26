package com.example.mediaservice.config;

import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class KafkaAvroRedisSerializer implements RedisSerializer<Object> {

    private final KafkaAvroDeserializer avroDeserializer;
    private final KafkaAvroSerializer avroSerializer;

    public KafkaAvroRedisSerializer() {
        // Cấu hình giống với Kafka consumer/producer
        Map<String, String> config = new HashMap<>();
        config.put("schema.registry.url", "http://localhost:8081"); // Thay bằng URL schema registry của bạn
        config.put("specific.avro.reader", "true");

        this.avroDeserializer = new KafkaAvroDeserializer();
        this.avroDeserializer.configure(config, false);

        this.avroSerializer = new KafkaAvroSerializer();
        this.avroSerializer.configure(config, false);
    }

    @Override
    public byte[] serialize(Object object) throws SerializationException {
        if (object == null) {
            return new byte[0];
        }

        try {
            // Serialize object thành Avro binary
            return avroSerializer.serialize("redis-topic", object);
        } catch (Exception e) {
            throw new SerializationException("Error serializing object with Kafka Avro Serializer", e);
        }
    }

    @Override
    public Object deserialize(byte[] bytes) throws SerializationException {
        if (bytes == null || bytes.length == 0) {
            return null;
        }

        try {
            // Deserialize Avro binary thành object
            return avroDeserializer.deserialize("redis-topic", bytes);
        } catch (Exception e) {
            throw new SerializationException("Error deserializing object with Kafka Avro Deserializer", e);
        }
    }
}
