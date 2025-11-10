package com.chatapp.Tej.dto;

import lombok.Builder;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

@Builder
public record DeliveryReceiptResponse(
        UUID messageId,  // ✅ String
        UUID chatId,       // ✅ UUID
        String username,
        Date deliveredAt
) {}
