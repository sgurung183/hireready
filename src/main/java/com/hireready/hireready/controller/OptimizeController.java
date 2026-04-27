package com.hireready.hireready.controller;

import com.hireready.hireready.dto.response.OptimizeResponse;
import com.hireready.hireready.entity.User;
import com.hireready.hireready.service.OptimizeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/api/optimize")
@RequiredArgsConstructor
public class OptimizeController {
    private final OptimizeService optimizeService;

    @GetMapping("/{resumeId}")
    public ResponseEntity<OptimizeResponse> optimize(
            @PathVariable Long resumeId,
            @AuthenticationPrincipal User currentUser,
            @RequestBody String jobDescription
    ){
        OptimizeResponse response = optimizeService.optimize(resumeId, jobDescription, currentUser);
    }
}
