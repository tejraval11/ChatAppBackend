package com.chatapp.Tej.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "chats")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Chat {

    @Id
    @GeneratedValue
    private UUID id;

    private boolean isGroup;
    private String name;

    private Date createdAt = new Date();

    @OneToMany(mappedBy = "chat", fetch = FetchType.LAZY)
    private List<ChatMember> members;
}
