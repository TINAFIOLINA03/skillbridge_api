package com.skillbridge.skillbridge_api.service;

import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.skillbridge.skillbridge_api.dto.DashboardResponse;
import com.skillbridge.skillbridge_api.entity.LearningItem;
import com.skillbridge.skillbridge_api.entity.LearningItemStatus;
import com.skillbridge.skillbridge_api.entity.User;
import com.skillbridge.skillbridge_api.repository.LearningItemRepository;
import com.skillbridge.skillbridge_api.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final LearningItemRepository learningItemRepository;
    private final UserRepository userRepository;

    public DashboardResponse getDashboard() {
        User user = getCurrentUser();
        List<LearningItem> items = learningItemRepository.findByUserId(user.getId());

        long totalLearning = items.size();
        long appliedCount = items.stream()
                .filter(item -> item.getStatus() == LearningItemStatus.APPLIED)
                .count();
        long pendingCount = items.stream()
                .filter(item -> item.getStatus() == LearningItemStatus.PENDING)
                .count();

        return new DashboardResponse(totalLearning, appliedCount, pendingCount);
    }

    private User getCurrentUser() {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
