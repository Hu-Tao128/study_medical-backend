package com.studymedical.backend.domain.services;

import com.studymedical.backend.domain.entities.User;
import com.studymedical.backend.domain.repositories.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final String jwtSecret;

    public UserService(UserRepository userRepository, @Value("${app.jwt.secret}") String jwtSecret) {
        this.userRepository = userRepository;
        this.jwtSecret = jwtSecret;
    }

    @Transactional
    public User getOrCreateFromToken(String token) {
        Claims claims = parseToken(token);
        String authId = claims.getSubject();
        String email = claims.get("email", String.class);
        String name = claims.get("name", String.class);

        Optional<User> existingUser = userRepository.findByAuthId(authId);

        if (existingUser.isPresent()) {
            User user = existingUser.get();
            user.setEmail(email);
            user.setDisplayName(name);
            user.setLastLoginAt(LocalDateTime.now());
            return userRepository.save(user);
        }

        User newUser = User.builder()
                .id(authId)
                .authId(authId)
                .email(email != null ? email : "unknown@supabase.local")
                .displayName(name)
                .role(User.Role.STUDENT)
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .build();

        return userRepository.save(newUser);
    }

    @Transactional
    public User createOrUpdateUser(String id, String email, String displayName, String photoUrl) {
        Optional<User> existingUser = userRepository.findById(id);

        if (existingUser.isPresent()) {
            User user = existingUser.get();
            user.setEmail(email);
            user.setDisplayName(displayName);
            user.setPhotoUrl(photoUrl);
            user.setLastLoginAt(LocalDateTime.now());
            return userRepository.save(user);
        }

        User newUser = User.builder()
                .id(id)
                .email(email)
                .displayName(displayName)
                .photoUrl(photoUrl)
                .role(User.Role.STUDENT)
                .enabled(true)
                .build();

        return userRepository.save(newUser);
    }

    public Optional<User> findById(String id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findByAuthId(String authId) {
        return userRepository.findByAuthId(authId);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    private Claims parseToken(String token) {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        SecretKey key = Keys.hmacShaKeyFor(keyBytes);

        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
