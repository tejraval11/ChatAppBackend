package com.chatapp.Tej.service;

import com.chatapp.Tej.dto.UserStatusEvent;
import com.chatapp.Tej.entity.User;
import com.chatapp.Tej.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class UserPresenceService {

    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    // Track active sessions: username -> sessionId
    private final Map<String, String> activeSessions = new ConcurrentHashMap<>();

    // Track typing users: chatId -> Set<username>
    private final Map<UUID, Set<String>> typingUsers = new ConcurrentHashMap<>();

    @Transactional
    public void setUserOnline(String username, String sessionId) {
        User user = userRepository.findByUsername(username).orElseThrow();
        user.setStatus(User.OnlineStatus.ONLINE);
        user.setSessionId(sessionId);
        user.setLastSeen(new Date());
        userRepository.save(user);

        activeSessions.put(username, sessionId);

        broadcastUserStatus(user);
    }

    @Transactional
    public void setUserOffline(String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) return;
        user.setStatus(User.OnlineStatus.OFFLINE);
        user.setSessionId(null);
        user.setLastSeen(new Date());
        userRepository.save(user);

        activeSessions.remove(username);

        broadcastUserStatus(user);
    }

    @Transactional
    public void setUserAway(String username) {
        User user = userRepository.findByUsername(username).orElseThrow();
        user.setStatus(User.OnlineStatus.AWAY);
        user.setLastSeen(new Date());
        userRepository.save(user);

        broadcastUserStatus(user);
    }

    public boolean isUserOnline(String username) {
        return activeSessions.containsKey(username);
    }

    public void addTypingUser(UUID chatId, String username) {
        typingUsers.computeIfAbsent(chatId, k -> ConcurrentHashMap.newKeySet()).add(username);
    }

    public void removeTypingUser(UUID chatId, String username) {
        Set<String> typing = typingUsers.get(chatId);
        if (typing != null) {
            typing.remove(username);
            if (typing.isEmpty()) {
                typingUsers.remove(chatId);
            }
        }
    }

    public Set<String> getTypingUsers(UUID chatId) {
        return typingUsers.getOrDefault(chatId, new HashSet<>());
    }

    public void broadcastUserStatus(User user) {
        UserStatusEvent event = UserStatusEvent.builder()
                .username(user.getUsername())
                .status(user.getStatus().name())
                .timestamp(new Date())
                .build();

        // Broadcast to all users (they'll filter on client side)
        messagingTemplate.convertAndSend("/topic/user.status", event);
    }

    // chatId -> set of online usernames
    private final Map<UUID, Set<String>> chatOnlineUsers = new ConcurrentHashMap<>();

    public void joinChat(UUID chatId, String username) {
        chatOnlineUsers.computeIfAbsent(chatId, k -> ConcurrentHashMap.newKeySet()).add(username);
        broadcastChatPresence(chatId);
    }

    public void leaveChat(UUID chatId, String username) {
        Set<String> users = chatOnlineUsers.get(chatId);
        if (users != null) {
            users.remove(username);
            if (users.isEmpty()) chatOnlineUsers.remove(chatId);
        }
        broadcastChatPresence(chatId);
    }

    public Set<String> getOnlineUsers(UUID chatId) {
        return chatOnlineUsers.getOrDefault(chatId, Set.of());
    }

    public void broadcastChatPresence(UUID chatId) {
        messagingTemplate.convertAndSend(
                "/topic/chat." + chatId + ".presence",
                getOnlineUsers(chatId)
        );
    }

}