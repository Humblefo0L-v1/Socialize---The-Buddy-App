package com.socialize.geolocation.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {
    
    @Value("${kafka.topics.location-updates}")
    private String locationUpdatesTopic;
    
    @Value("${kafka.topics.location-batch-updates}")
    private String batchUpdatesTopic;
    
    @Bean
    public NewTopic locationUpdatesTopic() {
        return TopicBuilder.name(locationUpdatesTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }
    
    @Bean
    public NewTopic batchUpdatesTopic() {
        return TopicBuilder.name(batchUpdatesTopic)
                .partitions(2)
                .replicas(1)
                .build();
    }
}