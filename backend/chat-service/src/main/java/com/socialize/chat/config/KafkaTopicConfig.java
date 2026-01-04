package com.socialize.chat.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {
    
    @Value("${kafka.topics.new-message}")
    private String newMessageTopic;
    
    @Value("${kafka.topics.message-read}")
    private String messageReadTopic;
    
    @Value("${kafka.topics.typing-indicator}")
    private String typingIndicatorTopic;
    
    @Bean
    public NewTopic newMessageTopic() {
        return TopicBuilder.name(newMessageTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }
    
    @Bean
    public NewTopic messageReadTopic() {
        return TopicBuilder.name(messageReadTopic)
                .partitions(2)
                .replicas(1)
                .build();
    }
    
    @Bean
    public NewTopic typingIndicatorTopic() {
        return TopicBuilder.name(typingIndicatorTopic)
                .partitions(2)
                .replicas(1)
                .compact()  // Use log compaction for typing indicators
                .build();
    }
}