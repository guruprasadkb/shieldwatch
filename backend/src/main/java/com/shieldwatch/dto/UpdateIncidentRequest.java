package com.shieldwatch.dto;

import com.shieldwatch.model.enums.Severity;
import lombok.Data;

@Data
public class UpdateIncidentRequest {
    private String title;
    private String description;
    private Severity severity;
}
