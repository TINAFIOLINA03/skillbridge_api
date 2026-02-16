package com.skillbridge.skillbridge_api.service;

import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.skillbridge.skillbridge_api.dto.CreateLearningRequest;
import com.skillbridge.skillbridge_api.dto.LearningResponse;
import com.skillbridge.skillbridge_api.entity.LearningCategory;
import com.skillbridge.skillbridge_api.entity.LearningItem;
import com.skillbridge.skillbridge_api.entity.LearningItemStatus;
import com.skillbridge.skillbridge_api.entity.User;
import com.skillbridge.skillbridge_api.repository.LearningItemRepository;
import com.skillbridge.skillbridge_api.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LearningService {

    private final LearningItemRepository learningItemRepository;
    private final UserRepository userRepository;

    public LearningResponse create(CreateLearningRequest request) {
        User user = getCurrentUser();

        LearningItem item = new LearningItem();
        item.setTitle(request.getTitle());
        item.setStatus(LearningItemStatus.PENDING);
        item.setCategory(LearningCategory.valueOf(request.getCategory()));
        item.setUser(user);

        LearningItem saved = learningItemRepository.save(item);
        return toResponse(saved);
    }

    public List<LearningResponse> getAll() {
        User user = getCurrentUser();
        return learningItemRepository.findByUserId(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public LearningResponse getById(Long id) {
        User user = getCurrentUser();
        LearningItem item = learningItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Learning item not found"));
        if (!item.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }
        return toResponse(item);
    }

    public LearningResponse update(Long id, CreateLearningRequest request) {
        User user = getCurrentUser();
        LearningItem item = learningItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Learning item not found"));
        if (!item.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }
        item.setTitle(request.getTitle());
        if (request.getCategory() != null) {
            item.setCategory(LearningCategory.valueOf(request.getCategory()));
        }
        LearningItem saved = learningItemRepository.save(item);
        return toResponse(saved);
    }

    public void delete(Long id) {
        User user = getCurrentUser();
        LearningItem item = learningItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Learning item not found"));
        if (!item.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }
        learningItemRepository.delete(item);
    }

    private User getCurrentUser() {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private LearningResponse toResponse(LearningItem item) {
        return new LearningResponse(
                item.getId(),
                item.getTitle(),
                item.getStatus().name(),
                item.getCategory().name(),
                item.getCreatedAt()
        );
    }
}
