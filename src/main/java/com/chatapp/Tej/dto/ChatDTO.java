package com.chatapp.Tej.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatDTO {
    private UUID id;
    private boolean isGroup;
    private String name;
    private List<String> participants;
    private String lastMessage;
    private Date lastMessageTime;
}
