package com.aifinancecopilot.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class OpenAiClient {
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String model;
    private final String baseUrl;

    public OpenAiClient(ObjectMapper objectMapper,
                        @Value("${app.openai.api-key}") String apiKey,
                        @Value("${app.openai.model}") String model,
                        @Value("${app.openai.base-url}") String baseUrl) {
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.model = model;
        this.baseUrl = baseUrl;
    }

    public boolean isConfigured() {
        return StringUtils.hasText(apiKey);
    }

    public String generate(String instructions, String input) {
        if (!isConfigured()) {
            throw new IllegalStateException("OpenAI API key is not configured");
        }
        try {
            String body = objectMapper.writeValueAsString(Map.of(
                    "model", model,
                    "instructions", instructions,
                    "input", input
            ));
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/responses"))
                    .timeout(Duration.ofSeconds(45))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("OpenAI request failed with status " + response.statusCode());
            }
            JsonNode json = objectMapper.readTree(response.body());
            JsonNode outputText = json.get("output_text");
            if (outputText != null && outputText.isTextual()) {
                return outputText.asText();
            }
            return json.path("output").findValuesAsText("text").stream().findFirst()
                    .orElse("AI response did not include text output.");
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("OpenAI request was interrupted", exception);
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to generate AI response", exception);
        }
    }
}
