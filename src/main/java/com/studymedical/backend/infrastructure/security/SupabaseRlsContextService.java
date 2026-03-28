package com.studymedical.backend.infrastructure.security;

import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class SupabaseRlsContextService {

    private final EntityManager entityManager;

    public SupabaseRlsContextService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public void applyAuthenticatedUser(UUID authId) {
        String claims = "{\"sub\":\"" + authId + "\",\"role\":\"authenticated\"}";
        entityManager
                .createNativeQuery("select set_config('request.jwt.claims', :claims, true)")
                .setParameter("claims", claims)
                .getSingleResult();
    }
}
