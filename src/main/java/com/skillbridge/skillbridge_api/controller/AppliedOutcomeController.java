package com.skillbridge.skillbridge_api.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.skillbridge.skillbridge_api.dto.AppliedOutcomeResponse;
import com.skillbridge.skillbridge_api.dto.CreateAppliedOutcomeRequest;
import com.skillbridge.skillbridge_api.service.AppliedOutcomeService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/learning/{learningId}/apply")
@RequiredArgsConstructor
public class AppliedOutcomeController {

    private final AppliedOutcomeService appliedOutcomeService;

    @PostMapping
    public ResponseEntity<AppliedOutcomeResponse> apply(
            @PathVariable Long learningId,
            @RequestBody CreateAppliedOutcomeRequest request) {
        AppliedOutcomeResponse response = appliedOutcomeService.create(learningId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<AppliedOutcomeResponse>> getAll(@PathVariable Long learningId) {
        List<AppliedOutcomeResponse> responses = appliedOutcomeService.getByLearningId(learningId);
        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/{outcomeId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long learningId,
            @PathVariable Long outcomeId) {
        appliedOutcomeService.delete(learningId, outcomeId);
        return ResponseEntity.noContent().build();
    }
}
