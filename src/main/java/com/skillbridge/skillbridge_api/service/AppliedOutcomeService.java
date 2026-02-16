package com.skillbridge.skillbridge_api.service;

import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.skillbridge.skillbridge_api.dto.AppliedOutcomeResponse;
import com.skillbridge.skillbridge_api.dto.CreateAppliedOutcomeRequest;
import com.skillbridge.skillbridge_api.entity.AppliedOutcome;
import com.skillbridge.skillbridge_api.entity.LearningItem;
import com.skillbridge.skillbridge_api.entity.LearningItemStatus;
import com.skillbridge.skillbridge_api.entity.OutcomeType;
import com.skillbridge.skillbridge_api.entity.User;
import com.skillbridge.skillbridge_api.repository.AppliedOutcomeRepository;
import com.skillbridge.skillbridge_api.repository.LearningItemRepository;
import com.skillbridge.skillbridge_api.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AppliedOutcomeService {

    private final AppliedOutcomeRepository appliedOutcomeRepository;
    private final LearningItemRepository learningItemRepository;
    private final UserRepository userRepository;

    public AppliedOutcomeResponse create(Long learningId, CreateAppliedOutcomeRequest request) {
        User user = getCurrentUser();

        LearningItem item = learningItemRepository.findById(learningId)
                .orElseThrow(() -> new RuntimeException("Learning item not found"));

        if (!item.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }

        AppliedOutcome outcome = new AppliedOutcome();
        outcome.setDescription(request.getDescription());
        outcome.setType(OutcomeType.valueOf(request.getType()));
        outcome.setLearningItem(item);

        AppliedOutcome saved = appliedOutcomeRepository.save(outcome);

        item.setStatus(LearningItemStatus.APPLIED);
        learningItemRepository.save(item);

        return toResponse(saved);
    }

    public List<AppliedOutcomeResponse> getByLearningId(Long learningId) {
        User user = getCurrentUser();

        LearningItem item = learningItemRepository.findById(learningId)
                .orElseThrow(() -> new RuntimeException("Learning item not found"));

        if (!item.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }

        return appliedOutcomeRepository.findByLearningItemId(learningId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public void delete(Long learningId, Long outcomeId) {
        User user = getCurrentUser();

        LearningItem item = learningItemRepository.findById(learningId)
                .orElseThrow(() -> new RuntimeException("Learning item not found"));

        if (!item.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }

        AppliedOutcome outcome = appliedOutcomeRepository.findById(outcomeId)
                .orElseThrow(() -> new RuntimeException("Applied outcome not found"));

        if (!outcome.getLearningItem().getId().equals(learningId)) {
            throw new RuntimeException("Outcome does not belong to this learning item");
        }

        appliedOutcomeRepository.delete(outcome);

        // If no more outcomes remain, set learning back to PENDING
        long remaining = appliedOutcomeRepository.findByLearningItemId(learningId).size();
        if (remaining == 0) {
            item.setStatus(LearningItemStatus.PENDING);
            learningItemRepository.save(item);
        }
    }

    private User getCurrentUser() {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private AppliedOutcomeResponse toResponse(AppliedOutcome outcome) {
        return new AppliedOutcomeResponse(
                outcome.getId(),
                outcome.getDescription(),
                outcome.getType().name(),
                outcome.getCreatedAt()
        );
    }
}
