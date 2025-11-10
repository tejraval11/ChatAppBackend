package com.chatapp.Tej.dto;

import java.util.UUID;

public record TypingEvent(UUID chatId, String username, boolean isTyping) {}

