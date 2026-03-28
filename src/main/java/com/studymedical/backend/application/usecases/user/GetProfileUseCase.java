package com.studymedical.backend.application.usecases.user;

import com.studymedical.backend.domain.entities.User;
import com.studymedical.backend.domain.repositories.UserRepository;
import com.studymedical.backend.infrastructure.security.SupabaseRlsContextService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class GetProfileUseCase {

    private final UserRepository userRepository;
    private final SupabaseRlsContextService supabaseRlsContextService;

    public GetProfileUseCase(
            UserRepository userRepository,
            SupabaseRlsContextService supabaseRlsContextService
    ) {
        this.userRepository = userRepository;
        this.supabaseRlsContextService = supabaseRlsContextService;
    }

    @Transactional(readOnly = true)
    public Optional<User> execute(UUID authId) {
        supabaseRlsContextService.applyAuthenticatedUser(authId);
        return userRepository.findByAuthId(authId);
    }
}
