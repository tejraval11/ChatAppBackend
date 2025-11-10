package com.chatapp.Tej.dto;

import java.util.UUID;

public record DeliveryReceiptRequest(UUID chatId, UUID messageId) {}

