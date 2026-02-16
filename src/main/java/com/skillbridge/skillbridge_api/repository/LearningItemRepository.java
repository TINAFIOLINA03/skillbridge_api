package com.skillbridge.skillbridge_api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.skillbridge.skillbridge_api.entity.LearningItem;

public interface LearningItemRepository extends JpaRepository<LearningItem, Long> {

    List<LearningItem> findByUserId(Long userId);
}
