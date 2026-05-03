package com.shieldwatch.dto;

import com.shieldwatch.model.enums.Status;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TransitionRequest {
    @NotNull(message = "Status is required")
    private Status status;
}
