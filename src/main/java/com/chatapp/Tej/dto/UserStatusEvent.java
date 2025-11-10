package com.chatapp.Tej.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatusEvent {
    private String username;
    private String status; // ONLINE, AWAY, OFFLINE
    private Date timestamp;
    private Set<UUID> activeChatIds; // Chats this user is in
}