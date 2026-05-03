package com.shieldwatch.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AssignRequest {
    @NotBlank(message = "Assignee ID is required")
    private String assigneeId;
}
