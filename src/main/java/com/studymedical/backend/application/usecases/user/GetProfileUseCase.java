package com.studymedical.backend.application.usecases.user;

import com.studymedical.backend.domain.entities.User;
import com.studymedical.backend.domain.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class GetProfileUseCase {

    private final UserRepository userRepository;

    public GetProfileUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> execute(UUID authId) {
        return userRepository.findByAuthId(authId);
    }
}
