package com.chatapp.Tej.dto;

import java.util.List;
import java.util.UUID;

public record CreateChatRequest(
        boolean isGroup,
        String name,
        List<UUID> memberIds
) {}
