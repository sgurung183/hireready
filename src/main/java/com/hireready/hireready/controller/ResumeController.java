package com.hireready.hireready.controller;

import com.hireready.hireready.dto.response.ResumeResponse;
import com.hireready.hireready.entity.User;
import com.hireready.hireready.service.ResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/resumes")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;

    @PostMapping("/upload")
    public ResponseEntity<ResumeResponse> uploadResume(
            @RequestParam("file") MultipartFile file, //pulls the file field out of the multipart form data
            @RequestParam(value = "isMain", defaultValue = "false") boolean isMain,
            @AuthenticationPrincipal User currentUser //spring security injects te currently logged in user directly fro m the SecurityContext holder
    ) {
        ResumeResponse resumeResponse = resumeService.uploadResume(file, isMain, currentUser);
        return ResponseEntity.ok(resumeResponse); //wrapping youre response so you can control the HTTP status code
    }

    @PutMapping("/setMain/{id}")
    public ResponseEntity<ResumeResponse> setMain(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser
    ){
        ResumeResponse response = resumeService.setResumeMain(id, currentUser);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResumeResponse> getResume(
        @PathVariable Long id,
        @AuthenticationPrincipal User currentUser
    ){
        ResumeResponse resumeResponse = resumeService.getResume(id, currentUser);
        return ResponseEntity.ok(resumeResponse);
    }

    @GetMapping("/findAll")
    public ResponseEntity<List<ResumeResponse>> findAllResume(
            @AuthenticationPrincipal User currentUser
    ){
        return ResponseEntity.ok(
                resumeService.getAllResume(currentUser)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteResume(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser
    ){
        resumeService.deleteResume(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}

