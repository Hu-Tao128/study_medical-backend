package com.studymedical.backend.infrastructure.security;

import com.studymedical.backend.application.usecases.auth.AuthUserPayload;
import com.studymedical.backend.application.usecases.user.CreateUserUseCase;
import com.studymedical.backend.domain.entities.User;
import com.studymedical.backend.domain.repositories.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.UUID;

@Service
public class RoleAuthorizationService {

    private final UserRepository userRepository;
    private final AuthTokenPayloadExtractor tokenPayloadExtractor;
    private final CreateUserUseCase createUserUseCase;

    public RoleAuthorizationService(
            UserRepository userRepository,
            AuthTokenPayloadExtractor tokenPayloadExtractor,
            CreateUserUseCase createUserUseCase
    ) {
        this.userRepository = userRepository;
        this.tokenPayloadExtractor = tokenPayloadExtractor;
        this.createUserUseCase = createUserUseCase;
    }

    public User requireAuthenticatedUser(Jwt jwt, String authHeader) {
        AuthUserPayload payload = tokenPayloadExtractor.extract(jwt, authHeader);
        UUID authId = createUserUseCase.normalizeAuthId(payload.subject());
        return userRepository.findByAuthId(authId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no sincronizado"));
    }

    public void requireAnyRole(User user, User.Role... roles) {
        boolean allowed = Arrays.stream(roles).anyMatch(role -> role == user.getRole());
        if (!allowed) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Rol sin permisos para esta operacion");
        }
    }

    public void requireSelfOrRole(User user, UUID targetUserId, User.Role... roles) {
        if (targetUserId != null && user.getId() != null && user.getId().equals(targetUserId)) {
            return;
        }
        requireAnyRole(user, roles);
    }
}
