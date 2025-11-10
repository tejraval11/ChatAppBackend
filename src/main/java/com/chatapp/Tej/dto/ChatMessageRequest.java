package com.chatapp.Tej.dto;

import java.util.UUID;

public record ChatMessageRequest(UUID chatId, String content) {
}
