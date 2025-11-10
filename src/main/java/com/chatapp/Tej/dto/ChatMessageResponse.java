package com.chatapp.Tej.dto;

import lombok.Builder;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

@Builder
public record ChatMessageResponse(
        UUID id,               // ✅ changed
        UUID chatId,             // ✅ Chat still UUID
        String sender,
        String content,
        Date createdAt,
        com.chatapp.Tej.entity.Message.MessageStatus status,
        Set<UUID> readBy,
        Set<UUID> deliveredTo
) {}
