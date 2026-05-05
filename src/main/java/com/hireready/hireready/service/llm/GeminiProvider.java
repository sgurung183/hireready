package com.hireready.hireready.service.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import com.hireready.hireready.dto.response.OptimizeResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class GeminiProvider implements LlmProvider {

    private static final Logger log = LoggerFactory.getLogger(GeminiProvider.class);

    @Value("${GOOGLE_API_KEY}")
    private String apiKey;

    @Override
    public OptimizeResponse optimize(String resumeContent, String jobDescription) {
        String prompt = getPrompt(resumeContent, jobDescription);

        log.info("Calling Gemini API with key prefix: {}...",
                apiKey != null && apiKey.length() > 8 ? apiKey.substring(0, 8) : "MISSING_OR_SHORT");

        try {
            Client client = Client.builder().apiKey(apiKey).build();

            // send the prompt to Gemini — model, prompt, null (no extra config needed)
            GenerateContentResponse response = client.models.generateContent(
                    "gemini-2.5-flash",
                    prompt,
                    null
            );

            // extract the raw JSON string from Gemini's response
            String responseText = response.text().trim();

            // Gemini sometimes wraps its response in markdown code fences like:
            //   ```json
            //   { ... }
            //   ```
            // even when told not to. We strip those fences so the JSON parser doesn't crash.
            // First replaceAll:  ^ means start of string — delete ``` and any letters after it (e.g. "json") and the newline
            // Second replaceAll: $ means end of string   — delete the closing ``` at the very end
            if (responseText.startsWith("```")) {
                responseText = responseText.replaceAll("^```[a-zA-Z]*\\n?", "").replaceAll("```$", "").trim();
            }

            // parse the JSON string into a tree so we can pull out individual fields
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode node = objectMapper.readTree(responseText);

            // build and return the response DTO with the parsed fields
            return OptimizeResponse.builder()
                    .optimizedText(node.get("optimizedText").asText())
                    .matchScore(node.get("matchScore").asDouble())
                    .createdAt(LocalDateTime.now())
                    .build();
        }
        catch(Exception e){
            throw new RuntimeException("Failed to call Gemini API: " + e.getMessage(), e);
        }
    }

    private String getPrompt(String resumeContent, String jobDescription) {
        // Java text block (triple quotes) — lets us write a multiline string cleanly
        // without concatenation. %s are placeholders that .formatted() replaces in order:
        // first %s → resumeContent, second %s → jobDescription.
        // We tell Gemini to return JSON only so we can parse the response programmatically.
        return """
                You are a resume optimizer. Given the resume and job description below, return a JSON object with:
                - "optimizedText": the resume rewritten and tailored to the job description
                - "matchScore": a number from 0 to 100 indicating how well the optimized resume matches the job

                Resume:
                %s

                Job Description:
                %s

                Return only valid JSON, no markdown.
                """.formatted(resumeContent, jobDescription);
    }
}
