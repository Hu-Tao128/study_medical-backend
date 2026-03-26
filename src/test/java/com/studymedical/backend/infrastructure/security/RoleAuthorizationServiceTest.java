package com.studymedical.backend.infrastructure.security;

import com.studymedical.backend.application.usecases.user.CreateUserUseCase;
import com.studymedical.backend.domain.entities.Membership;
import com.studymedical.backend.domain.entities.User;
import com.studymedical.backend.domain.repositories.MembershipRepository;
import com.studymedical.backend.domain.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoleAuthorizationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private MembershipRepository membershipRepository;

    @Mock
    private AuthTokenPayloadExtractor authTokenPayloadExtractor;

    @Mock
    private CreateUserUseCase createUserUseCase;

    private RoleAuthorizationService roleAuthorizationService;

    @BeforeEach
    void setUp() {
        roleAuthorizationService = new RoleAuthorizationService(
                userRepository,
                membershipRepository,
                authTokenPayloadExtractor,
                createUserUseCase
        );
    }

    @Test
    void shouldAllowTeacherWhenMembershipRoleIsTeacher() {
        UUID userId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();

        User teacher = User.builder().id(userId).role(User.Role.TEACHER).build();
        Membership membership = Membership.builder().role(Membership.Role.TEACHER).build();

        when(membershipRepository.findByUser_IdAndGroup_Id(userId, groupId)).thenReturn(Optional.of(membership));

        assertDoesNotThrow(() -> roleAuthorizationService.requireGroupTeacherOrAdmin(teacher, groupId));
    }

    @Test
    void shouldRejectTeacherWhenMembershipRoleIsStudent() {
        UUID userId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();

        User teacher = User.builder().id(userId).role(User.Role.TEACHER).build();
        Membership membership = Membership.builder().role(Membership.Role.STUDENT).build();

        when(membershipRepository.findByUser_IdAndGroup_Id(userId, groupId)).thenReturn(Optional.of(membership));

        assertThrows(ResponseStatusException.class, () -> roleAuthorizationService.requireGroupTeacherOrAdmin(teacher, groupId));
    }

    @Test
    void shouldAllowAdminWithoutMembershipLookup() {
        User admin = User.builder().id(UUID.randomUUID()).role(User.Role.ADMIN).build();
        assertDoesNotThrow(() -> roleAuthorizationService.requireGroupTeacherOrAdmin(admin, UUID.randomUUID()));
    }
}
