package com.shieldwatch.dto;

import com.shieldwatch.model.enums.Severity;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateIncidentRequest {
    private String title;
    private String description;
    @NotNull(message = "Severity is required")
    private Severity severity;
    private String teamId;
}
