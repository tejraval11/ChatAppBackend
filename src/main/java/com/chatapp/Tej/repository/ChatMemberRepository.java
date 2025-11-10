package com.chatapp.Tej.repository;

import com.chatapp.Tej.entity.Chat;
import com.chatapp.Tej.entity.ChatMember;
import com.chatapp.Tej.entity.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChatMemberRepository extends JpaRepository<ChatMember, UUID> {

    Optional<ChatMember> findByChatIdAndUserId(UUID chatId, UUID userId);

    List<ChatMember> findByChatId(UUID chatId);

    @Query("SELECT u FROM ChatMember cm JOIN cm.user u WHERE cm.chat.id = :chatId")
    List<User> findUsersInChat(UUID chatId);

    @Query("SELECT c FROM ChatMember cm JOIN cm.chat c WHERE cm.user.id = :userId")
    List<Chat> findChatsByUser(UUID userId);

    /**
     * Adds a user to chat (if not already in)
     */
    @Transactional
    default void addUserToChat(Chat chat, User user) {
        if (findByChatIdAndUserId(chat.getId(), user.getId()).isEmpty()) {
            ChatMember cm = new ChatMember();
            cm.setChat(chat);
            cm.setUser(user);
            save(cm);
        }
    }

}