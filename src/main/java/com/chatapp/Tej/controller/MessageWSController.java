package com.chatapp.Tej.controller;

import com.chatapp.Tej.dto.*;
import com.chatapp.Tej.entity.Chat;
import com.chatapp.Tej.entity.Message;
import com.chatapp.Tej.entity.User;
import com.chatapp.Tej.repository.ChatRepository;
import com.chatapp.Tej.repository.MessageRepository;
import com.chatapp.Tej.repository.UserRepository;
import com.chatapp.Tej.service.UserPresenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashSet;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class MessageWSController {

    private final SimpMessagingTemplate messagingTemplate;
    private final MessageRepository messageRepo;
    private final ChatRepository chatRepo;
    private final UserRepository userRepo;
    private final UserPresenceService presenceService;

    /**
     * Send message
     */
    @MessageMapping("/chat.sendMessage")
    @Transactional
    public void sendMessage(@Payload ChatMessageRequest req, Principal principal) {
        if (principal == null) return;

        try {
            User sender = userRepo.findByUsername(principal.getName())
                    .orElseThrow(() -> new RuntimeException("Sender not found"));

            UUID chatId = req.chatId();
            Chat chat = chatRepo.findById(chatId)
                    .orElseThrow(() -> new RuntimeException("Chat not found"));

            Message message = new Message();
            message.setChat(chat);
            message.setSender(sender);
            message.setContent(req.content());
            message.setStatus(Message.MessageStatus.SENT);
            message.setReadBy(new HashSet<>());
            message.setDeliveredTo(new HashSet<>());
            message.setCreatedAt(new Date());
            message = messageRepo.save(message);

            ChatMessageResponse res = ChatMessageResponse.builder()
                    .id(message.getId()) // ✅ String
                    .chatId(chat.getId()) // ✅ UUID
                    .sender(sender.getUsername())
                    .content(message.getContent())
                    .createdAt(message.getCreatedAt())
                    .status(message.getStatus())
                    .readBy(message.getReadBy().stream().map(User::getId).collect(Collectors.toSet()))
                    .deliveredTo(message.getDeliveredTo().stream().map(User::getId).collect(Collectors.toSet()))
                    .build();

            messagingTemplate.convertAndSend("/topic/chat." + chatId, res);

        } catch (Exception e) {
            System.err.println("[WS] sendMessage ERROR: " + e.getMessage());
        }
    }

    /**
     * Typing indicator
     */
    @MessageMapping("/chat.typing")
    public void handleTyping(@Payload TypingEvent event, Principal principal) {
        if (principal == null) return;

        String username = principal.getName();
        if (event.isTyping()) {
            presenceService.addTypingUser(event.chatId(), username);
        } else {
            presenceService.removeTypingUser(event.chatId(), username);
        }

        messagingTemplate.convertAndSend(
                "/topic/chat." + event.chatId() + ".typing",
                new TypingEvent(event.chatId(), username, event.isTyping())
        );
    }

    /**
     * Read receipt
     */
    @MessageMapping("/chat.read")
    @Transactional
    public void markAsRead(@Payload ReadReceiptRequest req, Principal principal) {
        if (principal == null) return;

        try {
            User user = userRepo.findByUsername(principal.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Message message = messageRepo.findByIdWithStatus(req.messageId()) // ✅ String ID
                    .orElseThrow(() -> new RuntimeException("Message not found"));

            message.getReadBy().add(user);
            message.setStatus(Message.MessageStatus.READ);
            messageRepo.save(message);

            ReadReceiptResponse res = ReadReceiptResponse.builder()
                    .messageId(message.getId()) // ✅ String
                    .chatId(message.getChat().getId()) // ✅ UUID
                    .username(user.getUsername())
                    .readAt(new Date())
                    .build();

            messagingTemplate.convertAndSend(
                    "/topic/chat." + message.getChat().getId() + ".read",
                    res
            );

        } catch (Exception e) {
            System.err.println("[WS] markAsRead ERROR: " + e.getMessage());
        }
    }

    /**
     * Delivery receipt
     */
    @MessageMapping("/chat.delivered")
    @Transactional
    public void markAsDelivered(@Payload DeliveryReceiptRequest req, Principal principal) {
        if (principal == null) return;

        try {
            User user = userRepo.findByUsername(principal.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Message message = messageRepo.findByIdWithStatus(req.messageId()) // ✅ String
                    .orElseThrow(() -> new RuntimeException("Message not found"));

            message.getDeliveredTo().add(user);
            if (message.getStatus() == Message.MessageStatus.SENT) {
                message.setStatus(Message.MessageStatus.DELIVERED);
            }
            messageRepo.save(message);

            DeliveryReceiptResponse res = DeliveryReceiptResponse.builder()
                    .messageId(message.getId()) // ✅ String
                    .chatId(message.getChat().getId()) // ✅ UUID
                    .username(user.getUsername())
                    .deliveredAt(new Date())
                    .build();

            messagingTemplate.convertAndSend(
                    "/topic/chat." + message.getChat().getId() + ".delivered",
                    res
            );

        } catch (Exception e) {
            System.err.println("[WS] markAsDelivered ERROR: " + e.getMessage());
        }
    }

    /**
     * Presence connect
     */
    @MessageMapping("/user.connect")
    public void userConnected(Principal principal, StompHeaderAccessor headerAccessor) {
        if (principal == null) return;
        presenceService.setUserOnline(principal.getName(), headerAccessor.getSessionId());
    }

    /**
     * Presence away
     */
    @MessageMapping("/user.away")
    public void userAway(Principal principal) {
        if (principal == null) return;
        presenceService.setUserAway(principal.getName());
    }

    @MessageMapping("/chat.join")
    public void joinChat(@Payload UUID chatId, Principal principal) {
        if (principal == null) return;
        presenceService.joinChat(chatId, principal.getName());
    }

    @MessageMapping("/chat.leave")
    public void leaveChat(@Payload UUID chatId, Principal principal) {
        if (principal == null) return;
        presenceService.leaveChat(chatId, principal.getName());
    }

    @MessageMapping("/chat.sync")
    public void syncPresence(@Payload UUID chatId, Principal principal) {
        if (principal == null) return;
        presenceService.broadcastChatPresence(chatId);
    }

}
