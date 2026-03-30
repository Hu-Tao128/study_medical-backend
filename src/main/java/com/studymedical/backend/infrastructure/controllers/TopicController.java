package com.studymedical.backend.infrastructure.controllers;

import com.studymedical.backend.domain.entities.Topic;
import com.studymedical.backend.domain.repositories.TopicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/topics")
@RequiredArgsConstructor
public class TopicController {

    private final TopicRepository topicRepository;

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> listTopics() {
        List<Topic> topics = topicRepository.findAll();
        List<Map<String, Object>> response = topics.stream()
                .map(topic -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", topic.getId().toString());
                    map.put("name", topic.getName() != null ? topic.getName() : "Sin nombre");
                    return map;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
}
