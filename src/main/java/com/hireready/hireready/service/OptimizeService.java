package com.hireready.hireready.service;

import com.hireready.hireready.entity.OptimizationResult;
import com.hireready.hireready.entity.Resume;
import com.hireready.hireready.entity.User;
import com.hireready.hireready.repository.OptimizationResultRepository;
import com.hireready.hireready.repository.ResumeRepository;
import com.hireready.hireready.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Service
@RequiredArgsConstructor
public class OptimizeService {

    private final OptimizationResultRepository optimizationResultRepository;
    private final ResumeRepository resumeRepository;

    public OptimizationResult optimize(Long resumeId, String request, User currentUser){
        Resume resume = resumeRepository.findByIdAndUserId(resumeId, currentUser.getId())
                .orElseThrow( () -> new RuntimeException("Resume cannot be found"));
        String content = resume.getContent();

    }

}
