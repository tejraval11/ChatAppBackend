package com.chatapp.Tej.repository;

import com.chatapp.Tej.entity.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface ChatRepository extends JpaRepository<Chat, UUID> {
    @Query("SELECT DISTINCT cm.chat FROM ChatMember cm WHERE cm.user.username = :username")
    List<Chat> findChatsByUser(String username);
}
