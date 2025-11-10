package com.chatapp.Tej.controller;

import com.chatapp.Tej.dto.ChatDTO;
import com.chatapp.Tej.dto.ChatMessageResponse;
import com.chatapp.Tej.dto.CreateChatRequest;
import com.chatapp.Tej.entity.Chat;
import com.chatapp.Tej.entity.Message;
import com.chatapp.Tej.entity.User;
import com.chatapp.Tej.repository.ChatMemberRepository;
import com.chatapp.Tej.repository.ChatRepository;
import com.chatapp.Tej.repository.MessageRepository;
import com.chatapp.Tej.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
public class ChatController {
    private final ChatRepository chatRepo;
    private final ChatMemberRepository chatMemberRepo;
    private final MessageRepository messageRepo;
    private final UserRepository userRepo;

    @PostMapping
    public ResponseEntity<Chat> createChat(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody CreateChatRequest req
    ) {
        if (userDetails == null) return ResponseEntity.status(401).build();

        // build and save chat
        Chat chatToSave = new Chat();
        chatToSave.setGroup(req.isGroup());
        if (req.isGroup()) chatToSave.setName(req.name());

        Chat savedChat = chatRepo.save(chatToSave); // savedChat is effectively final

        // add creator
        User creator = userRepo.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Creator not found"));
        chatMemberRepo.addUserToChat(savedChat, creator);

        // add selected members (without mutating req)
        List<UUID> members = req.memberIds();
        if (members != null && !members.isEmpty()) {
            for (UUID memberId : members) {
                // skip adding creator again
                if (memberId.equals(creator.getId())) continue;

                userRepo.findById(memberId).ifPresent(user ->
                        chatMemberRepo.addUserToChat(savedChat, user)
                );
            }
        }

        return ResponseEntity.ok(savedChat);
    }




    @GetMapping("/my")
    public ResponseEntity<List<ChatDTO>> myChats(@AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) return ResponseEntity.status(401).build();

        List<Chat> chats = chatRepo.findChatsByUser(userDetails.getUsername());

        List<ChatDTO> response = chats.stream()
                .map(c -> {
                    List<String> participants = chatMemberRepo.findUsersInChat(c.getId())
                            .stream()
                            .map(User::getUsername)
                            .toList();

                    return ChatDTO.builder()
                            .id(c.getId())
                            .isGroup(c.isGroup())
                            .name(c.getName())
                            .participants(participants)
                            .lastMessage(null)      // TODO: we will fetch later
                            .lastMessageTime(null)  // TODO: we will fetch later
                            .build();
                })
                .toList();

        return ResponseEntity.ok(response);
    }


    @GetMapping
    public List<Chat> getAllChats(){
        return chatRepo.findAll();
    }

    @PostMapping("/{chatId}/members")
    public ResponseEntity<?> addMember(
            @PathVariable UUID chatId,
            @RequestBody Map<String, String> request
    ) {
        String username = request.get("username");
        if (username == null || username.isEmpty()) {
            return ResponseEntity.badRequest().body("Username is required");
        }

        Chat chat = chatRepo.findById(chatId)
                .orElseThrow(() -> new RuntimeException("Chat not found"));

        User userToAdd = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        chatMemberRepo.addUserToChat(chat, userToAdd);

        return ResponseEntity.ok("User added to chat");
    }



    @GetMapping("/{chatId}/messages")
    public ResponseEntity<Page<ChatMessageResponse>> getMessages(
            @PathVariable UUID chatId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }

        try {
            PageRequest pageRequest = PageRequest.of(
                    page,
                    size,
                    Sort.by("createdAt").ascending()
            );

            Page<Message> messages = messageRepo.findByChatId(chatId, pageRequest);

            Page<ChatMessageResponse> response = messages.map(msg ->
                    ChatMessageResponse.builder()
                            .id(msg.getId())
                            .chatId(msg.getChat().getId())
                            .sender(msg.getSender().getUsername())
                            .content(msg.getContent())
                            .createdAt(msg.getCreatedAt())
                            .status(msg.getStatus())
                            .readBy(msg.getReadBy().stream().map(User::getId).collect(Collectors.toSet()))
                            .deliveredTo(msg.getDeliveredTo().stream().map(User::getId).collect(Collectors.toSet()))
                            .build()
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }
}
