package com.studymedical.backend.domain.repositories;

import com.studymedical.backend.domain.entities.StudyGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface StudyGroupRepository extends JpaRepository<StudyGroup, UUID> {

    List<StudyGroup> findByInstitution_Id(UUID institutionId);

    List<StudyGroup> findByCreatedBy_Id(UUID createdBy);
}
