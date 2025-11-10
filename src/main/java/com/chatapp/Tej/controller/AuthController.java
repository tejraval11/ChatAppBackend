package com.chatapp.Tej.controller;

import com.chatapp.Tej.dto.AuthResponse;
import com.chatapp.Tej.dto.LoginRequest;
import com.chatapp.Tej.dto.RegisterRequest;
import com.chatapp.Tej.entity.User;
import com.chatapp.Tej.repository.UserRepository;
import com.chatapp.Tej.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.swing.text.html.Option;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin
public class AuthController {

    private final UserRepository userRepo;
    private final JwtService jwtService;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @PostMapping("/register")
    public AuthResponse register(@RequestBody RegisterRequest request){
        User user = new User();
        user.setUsername(request.username());
        user.setPassword(encoder.encode(request.password()));
        userRepo.save(user);

        return new AuthResponse(jwtService.generateToken(user.getUsername()));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

        Optional<User> userOpt = userRepo.findByUsername(request.username());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid username or password");
        }

        User user = userOpt.get();
        if (!encoder.matches(request.password(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid username or password");
        }

        // generate token using actual stored username
        String token = jwtService.generateToken(user.getUsername());
        return ResponseEntity.ok(new AuthResponse(token));
    }

}
