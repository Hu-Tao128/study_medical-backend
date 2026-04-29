package com.studymedical.backend.infrastructure.security;

import com.studymedical.backend.application.usecases.auth.AuthUserPayload;
import com.studymedical.backend.application.usecases.user.CreateUserUseCase;
import com.studymedical.backend.domain.entities.Membership;
import com.studymedical.backend.domain.entities.User;
import com.studymedical.backend.domain.repositories.MembershipRepository;
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
    private final MembershipRepository membershipRepository;
    private final AuthTokenPayloadExtractor tokenPayloadExtractor;
    private final CreateUserUseCase createUserUseCase;

    public RoleAuthorizationService(
            UserRepository userRepository,
            MembershipRepository membershipRepository,
            AuthTokenPayloadExtractor tokenPayloadExtractor,
            CreateUserUseCase createUserUseCase
    ) {
        this.userRepository = userRepository;
        this.membershipRepository = membershipRepository;
        this.tokenPayloadExtractor = tokenPayloadExtractor;
        this.createUserUseCase = createUserUseCase;
    }

    public User requireAuthenticatedUser(Jwt jwt, String authHeader) {
        AuthUserPayload payload = tokenPayloadExtractor.extract(jwt, authHeader);
        UUID authId = createUserUseCase.normalizeAuthId(payload.subject());
        return userRepository.findByAuthId(authId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no sincronizado"));
    }

    public User requireAuthenticatedUser(AuthenticatedUser principal) {
        return userRepository.findById(principal.userId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));
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

    public void requireGroupAccess(User user, UUID groupId) {
        Membership membership = resolveMembership(user, groupId);
        if (membership == null && user.getRole() != User.Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No pertenece al grupo");
        }
    }

    public void requireGroupTeacherOrAdmin(User user, UUID groupId) {
        if (user.getRole() == User.Role.ADMIN) {
            return;
        }

        requireAnyRole(user, User.Role.TEACHER);

        Membership membership = resolveMembership(user, groupId);
        if (membership == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No pertenece al grupo");
        }

        if (membership.getRole() != Membership.Role.TEACHER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Debe ser teacher en el grupo para crear contenido grupal");
        }
    }

    private Membership resolveMembership(User user, UUID groupId) {
        if (groupId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "groupId es requerido para visibilidad GROUP");
        }

        if (user.getRole() == User.Role.ADMIN) {
            return null;
        }

        UUID userId = user.getId();
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario sin id interno");
        }

        return membershipRepository.findByUser_IdAndGroup_Id(userId, groupId).orElse(null);
    }

    public boolean canReadByVisibility(User user, UUID contentOwnerId, QuizVisibility visibility, UUID groupId) {
        if (visibility == null || visibility == QuizVisibility.PUBLIC) {
            return true;
        }

        if (user.getRole() == User.Role.ADMIN) {
            return true;
        }

        UUID userId = user.getId();
        if (userId == null) {
            return false;
        }

        if (visibility == QuizVisibility.PRIVATE) {
            return contentOwnerId != null && contentOwnerId.equals(userId);
        }

        return groupId != null && membershipRepository.findByUser_IdAndGroup_Id(userId, groupId).isPresent();
    }

    public enum QuizVisibility {
        PRIVATE,
        GROUP,
        PUBLIC
    }
}
