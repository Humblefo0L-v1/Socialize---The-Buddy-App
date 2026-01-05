package com.socialize.request.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {
    
    @Value("${kafka.topics.request-created}")
    private String requestCreatedTopic;
    
    @Value("${kafka.topics.request-approved}")
    private String requestApprovedTopic;
    
    @Value("${kafka.topics.request-declined}")
    private String requestDeclinedTopic;
    
    @Value("${kafka.topics.request-cancelled}")
    private String requestCancelledTopic;
    
    @Bean
    public NewTopic requestCreatedTopic() {
        return TopicBuilder.name(requestCreatedTopic)
                .partitions(2)
                .replicas(1)
                .build();
    }
    
    @Bean
    public NewTopic requestApprovedTopic() {
        return TopicBuilder.name(requestApprovedTopic)
                .partitions(2)
                .replicas(1)
                .build();
    }
    
    @Bean
    public NewTopic requestDeclinedTopic() {
        return TopicBuilder.name(requestDeclinedTopic)
                .partitions(2)
                .replicas(1)
                .build();
    }
    
    @Bean
    public NewTopic requestCancelledTopic() {
        return TopicBuilder.name(requestCancelledTopic)
                .partitions(2)
                .replicas(1)
                .build();
    }
}