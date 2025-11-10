package com.chatapp.Tej.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter @Setter @NoArgsConstructor
public class User implements UserDetails {
    @Id @GeneratedValue
    private UUID id;

    @Column(unique = true, nullable = false)
    private String username;

    private String password;

    // ✅ NEW: Online status tracking
    @Enumerated(EnumType.STRING)
    private OnlineStatus status = OnlineStatus.OFFLINE;

    // ✅ NEW: Last seen timestamp
    private Date lastSeen = new Date();

    // ✅ NEW: WebSocket session ID (for presence tracking)
    private String sessionId;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public enum OnlineStatus {
        ONLINE,
        AWAY,
        OFFLINE
    }
}