package com.skillbridge.skillbridge_api.dto;

import lombok.Data;

@Data
public class CreateLearningRequest {
    private String title;
    private String category;
}
