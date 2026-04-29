package com.studymedical.backend.infrastructure.services;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.stereotype.Service;

@Service
public class FirebaseAuthService {

    public FirebaseToken verify(String token) {
        try {
            return FirebaseAuth.getInstance().verifyIdToken(token);
        } catch (Exception e) {
            throw new RuntimeException("Invalid Firebase token", e);
        }
    }
}
