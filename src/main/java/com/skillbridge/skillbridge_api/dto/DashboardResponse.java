package com.skillbridge.skillbridge_api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DashboardResponse {
    private long totalLearning;
    private long appliedCount;
    private long pendingCount;
}
