package com.chatapp.Tej.dto;

import java.util.UUID;

public record ReadReceiptRequest(UUID chatId, UUID messageId) {}

