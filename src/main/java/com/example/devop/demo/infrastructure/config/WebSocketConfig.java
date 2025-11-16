package com.example.devop.demo.infrastructure.config;

import com.example.devop.demo.shared.utils.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

import java.util.Collections;
import java.util.List;

@Configuration
@EnableWebSocketMessageBroker
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${websocket.allowed-origins:*}")
    private String allowedOrigins;

    @Value("${websocket.message-size-limit:128}")
    private Integer messageSizeLimit;

    @Value("${websocket.send-buffer-size-limit:512}")
    private Integer sendBufferSizeLimit;

    @Value("${websocket.send-time-limit:20000}")
    private Integer sendTimeLimit;

    private static final Logger logger = LoggerFactory.getLogger(WebSocketConfig.class);

    @Autowired
    private JwtDecoder jwtDecoder;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue");

        registry.setApplicationDestinationPrefixes("/app");

        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns(allowedOrigins.split(","))
                .withSockJS();

        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns(allowedOrigins.split(","));
    }

    @Override
    public void configureClientOutboundChannel(ChannelRegistration registration) {
        registration.taskExecutor()
                .corePoolSize(4)
                .maxPoolSize(8);
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor =
                        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    try {
                        List<String> authorization = accessor.getNativeHeader("Authorization");

                        if (authorization == null || authorization.isEmpty()) {
                            logger.warn("Missing Authorization header");
                            throw new IllegalArgumentException("Missing authorization");
                        }

                        String authHeader = authorization.get(0);
                        if (!authHeader.startsWith("Bearer ")) {
                            logger.warn("Invalid authorization format");
                            throw new IllegalArgumentException("Invalid format");
                        }

                        String token = authHeader.substring(7);

                        Jwt jwt = jwtDecoder.decode(token);
                        String userId = jwt.getClaimAsString(Claims.USER_ID);

                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(
                                        userId,
                                        null,
                                        Collections.emptyList()
                                );
                        accessor.setUser(authentication);

                        logger.info("WebSocket authenticated: userId={}", userId);

                    } catch (Exception e) {
                        logger.error("WebSocket authentication failed: {}", e.getMessage());
                        throw new IllegalArgumentException("Authentication failed", e);
                    }
                }

                return message;
            }
        });
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        registration.setMessageSizeLimit(messageSizeLimit * 1024);
        registration.setSendBufferSizeLimit(sendBufferSizeLimit * 1024);
        registration.setSendTimeLimit(sendTimeLimit);
    }
}
