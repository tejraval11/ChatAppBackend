package com.chatapp.Tej.security;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;

import java.security.Principal;

public class JwtAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;

    public JwtAuthChannelInterceptor(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = accessor.getFirstNativeHeader("Authorization");

            System.out.println("[WS] CONNECT attempt with token: " + (token != null ? "present" : "missing"));
            System.out.println("[WS] All headers: " + accessor.toNativeHeaderMap());

            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);

                try {
                    String username = jwtService.extractUsername(token);

                    if (username != null) {
                        System.out.println("[WS] ✅ Principal set during CONNECT: " + username);
                        Principal principal = new StompPrincipal(username);
                        accessor.setUser(principal);
                        // Store username in session for later retrieval
                        accessor.getSessionAttributes().put("username", username);
                    } else {
                        System.err.println("[WS] ❌ Username extraction returned null");
                        return null;
                    }
                } catch (Exception e) {
                    System.err.println("[WS] ❌ JWT validation failed: " + e.getMessage());
                    e.printStackTrace();
                    return null; // Reject invalid tokens
                }
            } else {
                System.err.println("[WS] ❌ CONNECT without valid Authorization header");
                System.err.println("[WS] Token value: " + token);
                return null; // Reject connections without token
            }
        }

        // For SEND frames, check if user exists
        if (accessor != null && StompCommand.SEND.equals(accessor.getCommand())) {
            Principal user = accessor.getUser();

            if (user == null) {
                System.err.println("[WS] SEND without Principal — rejected");
                return null;
            }

            System.out.println("[WS] SEND from user: " + user.getName());
        }

        return message;
    }
}