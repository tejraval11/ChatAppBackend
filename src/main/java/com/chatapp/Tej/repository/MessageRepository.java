package com.chatapp.Tej.repository;

import com.chatapp.Tej.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {

    /**
     * Find message with eagerly loaded status collections
     */
    @Query("SELECT m FROM Message m " +
            "LEFT JOIN FETCH m.readBy " +
            "LEFT JOIN FETCH m.deliveredTo " +
            "LEFT JOIN FETCH m.sender " +
            "LEFT JOIN FETCH m.chat " +
            "WHERE m.id = :id")
    Optional<Message> findByIdWithStatus(@Param("id") UUID id);

    /**
     * Find messages by chat with eagerly loaded collections
     */
    @Query("SELECT DISTINCT m FROM Message m " +
            "LEFT JOIN FETCH m.readBy " +
            "LEFT JOIN FETCH m.deliveredTo " +
            "LEFT JOIN FETCH m.sender " +
            "WHERE m.chat.id = :chatId " +
            "ORDER BY m.createdAt ASC")
    Page<Message> findByChatIdWithStatus(@Param("chatId") UUID chatId, Pageable pageable);

    /**
     * Regular findByChatId without eager loading (if you need it for other purposes)
     */
    Page<Message> findByChatId(UUID chatId, Pageable pageable);
}