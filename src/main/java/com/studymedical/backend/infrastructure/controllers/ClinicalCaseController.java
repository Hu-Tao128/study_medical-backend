package com.studymedical.backend.infrastructure.controllers;

import com.studymedical.backend.domain.entities.ClinicalCase;
import com.studymedical.backend.domain.repositories.mongo.ClinicalCaseMongoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cases")
@RequiredArgsConstructor
public class ClinicalCaseController {

    private final ClinicalCaseMongoRepository clinicalCaseMongoRepository;

    @PostMapping
    public ResponseEntity<ClinicalCase> createCase(@RequestBody ClinicalCase clinicalCase) {
        clinicalCase.initializeDefaults();
        return ResponseEntity.ok(clinicalCaseMongoRepository.save(clinicalCase));
    }

    @GetMapping("/topic/{topicId}")
    public ResponseEntity<List<ClinicalCase>> getByTopic(@PathVariable UUID topicId) {
        return ResponseEntity.ok(clinicalCaseMongoRepository.findByTopicId(topicId));
    }
}
