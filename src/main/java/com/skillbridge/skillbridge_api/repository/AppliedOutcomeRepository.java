package com.skillbridge.skillbridge_api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.skillbridge.skillbridge_api.entity.AppliedOutcome;

public interface AppliedOutcomeRepository extends JpaRepository<AppliedOutcome, Long> {

    List<AppliedOutcome> findByLearningItemId(Long learningItemId);
}
