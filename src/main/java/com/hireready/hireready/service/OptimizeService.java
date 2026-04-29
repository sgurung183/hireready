package com.hireready.hireready.service;

import com.hireready.hireready.dto.request.OptimizeRequest;
import com.hireready.hireready.dto.response.OptimizeResponse;
import com.hireready.hireready.dto.response.ResumeResponse;
import com.hireready.hireready.entity.OptimizationResult;
import com.hireready.hireready.entity.Resume;
import com.hireready.hireready.entity.User;
import com.hireready.hireready.exception.ResourceNotFoundException;
import com.hireready.hireready.repository.OptimizationResultRepository;
import com.hireready.hireready.repository.ResumeRepository;
import com.hireready.hireready.service.llm.LlmProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OptimizeService {

    private final OptimizationResultRepository optimizationResultRepository;
    private final ResumeRepository resumeRepository;
    private final LlmProvider llmProvider;

    public OptimizeResponse optimize(Long resumeId, OptimizeRequest request, User currentUser){
        // fetch resume and verify it belongs to the current user
        Resume resume = resumeRepository.findByIdAndUserId(resumeId, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found"));

        // send resume content + job description to the LLM
        OptimizeResponse optimizeResponse = llmProvider.optimize(resume.getContent(), request.getJobDescriptionText());

        // save the result to the DB linked to this resume
        OptimizationResult result = OptimizationResult.builder()
                .resume(resume)
                .jobDescriptionText(request.getJobDescriptionText())
                .optimizedText(optimizeResponse.getOptimizedText())
                .matchScore(optimizeResponse.getMatchScore())
                .build();

        // save returns the persisted entity with the DB-generated id
        OptimizationResult savedResult = optimizationResultRepository.save(result);

        // build response from the saved entity so the id is populated
        return OptimizeResponse.builder()
                .id(savedResult.getId())
                .optimizedText(savedResult.getOptimizedText())
                .matchScore(savedResult.getMatchScore())
                .createdAt(savedResult.getCreatedAt())
                .build();
    }

    public List<OptimizeResponse> getHistory(Long resumeId, User currentUser){

        //first verify the resume belongs to the current user before fetching its history
        resumeRepository.findByIdAndUserId(resumeId, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found"));

        return optimizationResultRepository.findAllByResumeId(resumeId)
                .stream()
                .map(optimized -> OptimizeResponse.builder()
                        .id(optimized.getId())
                        .optimizedText(optimized.getOptimizedText())
                        .matchScore(optimized.getMatchScore())
                        .createdAt(optimized.getCreatedAt())
                        .build()
                )
                .toList();
    }

}
