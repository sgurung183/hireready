package com.hireready.hireready.service.llm;

import com.hireready.hireready.dto.response.OptimizeResponse;

public interface LlmProvider {
    OptimizeResponse optimize(String resumeContent, String jobDescription);
}
