package com.hireready.hireready.controller;

import com.hireready.hireready.dto.request.OptimizeRequest;
import com.hireready.hireready.dto.response.OptimizeResponse;
import com.hireready.hireready.entity.User;
import com.hireready.hireready.service.OptimizeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/optimize")
@RequiredArgsConstructor
public class OptimizeController {
    private final OptimizeService optimizeService;

    @PostMapping ("/{resumeId}")
    public ResponseEntity<OptimizeResponse> optimize(
            @PathVariable Long resumeId,
            @AuthenticationPrincipal User currentUser,
            @RequestBody @Valid OptimizeRequest request
            ){
        OptimizeResponse response = optimizeService.optimize(resumeId, request, currentUser);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{resumeId}/history")
    public ResponseEntity<List<OptimizeResponse>> viewOptimizedHistory(
            @PathVariable Long resumeId,
            @AuthenticationPrincipal User currentUser
    ){
        List<OptimizeResponse> history = optimizeService.getHistory(resumeId, currentUser);
        return ResponseEntity.ok(history);
    }
}
