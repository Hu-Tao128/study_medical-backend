package com.studymedical.backend.application.usecases.user;

import com.studymedical.backend.domain.entities.User;
import com.studymedical.backend.domain.repositories.UserRepository;
import com.studymedical.backend.infrastructure.security.SupabaseRlsContextService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class UpdateProfileUseCase {

    private final UserRepository userRepository;
    private final SupabaseRlsContextService supabaseRlsContextService;

    public UpdateProfileUseCase(
            UserRepository userRepository,
            SupabaseRlsContextService supabaseRlsContextService
    ) {
        this.userRepository = userRepository;
        this.supabaseRlsContextService = supabaseRlsContextService;
    }

    @Transactional
    public Optional<User> execute(UUID authId, UpdateProfileCommand command) {
        supabaseRlsContextService.applyAuthenticatedUser(authId);
        return userRepository.findByAuthId(authId)
                .map(user -> {
                    if (command.displayName() != null) {
                        user.setDisplayName(command.displayName());
                    }
                    if (command.photoUrl() != null) {
                        user.setPhotoUrl(command.photoUrl());
                    }
                    if (command.preferredLanguage() != null) {
                        user.setPreferredLanguage(command.preferredLanguage());
                    }
                    if (command.theme() != null) {
                        user.setTheme(command.theme());
                    }
                    if (command.level() != null) {
                        user.setLevel(command.level());
                    }
                    if (command.semester() != null) {
                        user.setSemester(command.semester());
                    }
                    if (command.career() != null) {
                        user.setCareer(command.career());
                    }
                    user.setLastActiveAt(LocalDateTime.now());
                    return userRepository.save(user);
                });
    }

    public record UpdateProfileCommand(
            String displayName,
            String photoUrl,
            String preferredLanguage,
            String theme,
            String level,
            Integer semester,
            String career
    ) {
    }
}
