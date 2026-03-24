package com.studymedical.backend.domain.repositories;

import com.studymedical.backend.domain.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByEmail(String email);

    Optional<User> findByAuthId(String authId);

    boolean existsByEmail(String email);
}
