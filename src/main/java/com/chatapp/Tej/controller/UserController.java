package com.chatapp.Tej.controller;

import com.chatapp.Tej.dto.UserSearchDTO;
import com.chatapp.Tej.entity.User;
import com.chatapp.Tej.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepo;

    @GetMapping("/search")
    public ResponseEntity<List<UserSearchDTO>> searchUsers(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String query
    ) {
        if (userDetails == null) return ResponseEntity.status(401).build();

        List<User> users = userRepo.searchUsers(query);

        List<UserSearchDTO> response = users.stream()
                .filter(u -> !u.getUsername().equals(userDetails.getUsername()))
                .map(u -> new UserSearchDTO(
                        u.getId(),
                        u.getUsername()
                ))
                .toList();

        return ResponseEntity.ok(response);
    }

}
