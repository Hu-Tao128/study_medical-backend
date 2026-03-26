package com.studymedical.backend.domain.repositories;

import com.studymedical.backend.domain.entities.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TopicRepository extends JpaRepository<Topic, UUID> {

    List<Topic> findByParent_Id(UUID parentId);
}
