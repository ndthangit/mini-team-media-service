package com.example.mediaservice.config;

import com.example.mediaservice.entity.Group;
import com.example.mediaservice.entity.User;
import com.example.mediaservice.entity.relationship.UserGroup;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.*;

import java.util.HashMap;
import java.util.Map;


@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.properties.schema.registry.url}")
    private String schemaRegistryUrl;

    @Bean
    public KafkaTemplate<String, User> userTemplate(ProducerFactory<String, User> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public KafkaTemplate<String, Group> groupTemplate(ProducerFactory<String, Group> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public KafkaTemplate<String, UserGroup> userGroupTemplate(ProducerFactory<String, UserGroup> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate(ProducerFactory<String, String> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    // Consumer Factory for Group
    @Bean
    public ConsumerFactory<String, Group> groupConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "media-service-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);
        props.put(KafkaAvroDeserializerConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
        props.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, true);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Group> groupKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Group> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(groupConsumerFactory());
        return factory;
    }

    // Consumer Factory for UserGroup
    @Bean
    public ConsumerFactory<String, UserGroup> userGroupConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "media-service-user-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);
        props.put(KafkaAvroDeserializerConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
        props.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, true);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, UserGroup> userGroupKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, UserGroup> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(userGroupConsumerFactory());
        return factory;
    }

//    @Bean
//    public ConcurrentMessageListenerContainer<String, User> repliesContainer(
//            ConsumerFactory<String, User> cf) {
//        ContainerProperties containerProperties = new ContainerProperties("get-user-reply");
//        containerProperties.setGroupId("get-user-reply-group");  // Set groupId here
//        return new ConcurrentMessageListenerContainer<>(cf, containerProperties);
//    }
//
//    @Bean
//    public ReplyingKafkaTemplate<String, String, User> replyingKafkaTemplate(
//            ProducerFactory<String, String> pf,
//            ConcurrentMessageListenerContainer<String, User> repliesContainer) {
//        return new ReplyingKafkaTemplate<>(pf, repliesContainer);
//    }

    @Bean
    public KafkaAdmin.NewTopics topics() {
        return new KafkaAdmin.NewTopics(
                TopicBuilder.name("user-update")
                        .partitions(3)
                        .replicas(1)
                        .build(),
                TopicBuilder.name("user-create")
                        .partitions(3)
                        .replicas(1)
                        .build(),
                TopicBuilder.name("group-created")
                        .partitions(3)
                        .replicas(1)
                        .build(),
                TopicBuilder.name("group-updated")
                        .partitions(3)
                        .replicas(1)
                        .build(),
                TopicBuilder.name("user-group-create")
                        .partitions(3)
                        .replicas(1)
                        .build(),
                TopicBuilder.name("user-group-join")
                        .partitions(3)
                        .replicas(1)
                        .build()
        );

    }

}
