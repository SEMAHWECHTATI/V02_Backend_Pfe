package com.sagem.g2ii.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Canal de diffusion : Angular s'abonnera aux messages commençant par /topic
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Point de connexion initial (Handshake) pour le frontend Angular
        registry.addEndpoint("/ws-notifications")
                .setAllowedOrigins("http://localhost:4200") // URL de ton app Angular
                .withSockJS(); // Option de secours si le protocole WebSocket pur est bloqué
    }
}