package com.skillbridge.skillbridge_api.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.skillbridge.skillbridge_api.dto.CreateLearningRequest;
import com.skillbridge.skillbridge_api.dto.LearningResponse;
import com.skillbridge.skillbridge_api.service.LearningService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/learning")
@RequiredArgsConstructor
public class LearningController {

    private final LearningService learningService;

    @PostMapping
    public ResponseEntity<LearningResponse> create(@RequestBody CreateLearningRequest request) {
        LearningResponse response = learningService.create(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<LearningResponse>> getAll() {
        List<LearningResponse> responses = learningService.getAll();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LearningResponse> getById(@PathVariable Long id) {
        LearningResponse response = learningService.getById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<LearningResponse> update(
            @PathVariable Long id,
            @RequestBody CreateLearningRequest request) {
        LearningResponse response = learningService.update(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        learningService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
