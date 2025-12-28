package com.socialize.event.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic eventNotificationsTopic() {
        return TopicBuilder.name("event-notifications")
                .partitions(3)
                .replicas(1)
                .build();
    }
}

