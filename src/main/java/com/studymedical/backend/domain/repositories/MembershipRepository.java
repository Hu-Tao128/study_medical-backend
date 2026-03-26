package com.studymedical.backend.domain.repositories;

import com.studymedical.backend.domain.entities.Membership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MembershipRepository extends JpaRepository<Membership, UUID> {

    List<Membership> findByUser_Id(UUID userId);

    List<Membership> findByGroup_Id(UUID groupId);

    Optional<Membership> findByUser_IdAndGroup_Id(UUID userId, UUID groupId);
}
