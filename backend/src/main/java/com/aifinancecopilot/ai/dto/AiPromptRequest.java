package com.aifinancecopilot.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AiPromptRequest(@NotBlank @Size(max = 500) String question) {}
