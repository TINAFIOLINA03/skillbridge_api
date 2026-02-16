package com.skillbridge.skillbridge_api.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LearningResponse {
    private Long id;
    private String title;
    private String status;
    private String category;
    private LocalDateTime createdAt;
}
