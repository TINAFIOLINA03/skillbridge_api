package com.skillbridge.skillbridge_api.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AppliedOutcomeResponse {
    private Long id;
    private String description;
    private String type;
    private LocalDateTime createdAt;
}
