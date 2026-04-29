package com.studymedical.backend.application.usecases.user;

import com.google.firebase.auth.FirebaseToken;
import com.studymedical.backend.application.usecases.auth.AuthUserPayload;
import com.studymedical.backend.domain.entities.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final CreateUserUseCase createUserUseCase;

    @Transactional
    public User getOrCreate(FirebaseToken decodedToken) {
        AuthUserPayload payload = new AuthUserPayload(
                decodedToken.getUid(),
                decodedToken.getEmail(),
                (String) decodedToken.getClaims().get("name"),
                (String) decodedToken.getClaims().get("picture")
        );

        return createUserUseCase.execute(payload);
    }
}
