package com.socialize.chat.config;

import com.socialize.chat.security.JwtChannelInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    
    private final JwtChannelInterceptor jwtChannelInterceptor;
    
    @Value("${websocket.allowed-origins}")
    private String allowedOrigins;
    
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Enable a simple in-memory message broker
        registry.enableSimpleBroker("/topic", "/queue");
        
        // Prefix for messages from client to server
        registry.setApplicationDestinationPrefixes("/app");
        
        // Prefix for user-specific messages
        registry.setUserDestinationPrefix("/user");
    }
    
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Register STOMP endpoint with SockJS fallback
        registry.addEndpoint("/ws/chat")
                .setAllowedOrigins(allowedOrigins.split(","))
                .withSockJS();
        
        // Register endpoint without SockJS for native WebSocket clients
        registry.addEndpoint("/ws/chat")
                .setAllowedOrigins(allowedOrigins.split(","));
    }
    
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // Add JWT authentication interceptor
        registration.interceptors(jwtChannelInterceptor);
    }
}