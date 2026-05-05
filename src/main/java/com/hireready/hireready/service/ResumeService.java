package com.hireready.hireready.service;

import com.hireready.hireready.dto.response.ResumeResponse;
import com.hireready.hireready.entity.Resume;
import com.hireready.hireready.entity.User;
import com.hireready.hireready.exception.ResourceNotFoundException;
import com.hireready.hireready.repository.ResumeRepository;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ResumeService {
    private final ResumeRepository resumeRepository;

    public List<ResumeResponse> getAllResume(User currentUser){
        // alternative implementation using forEach and manual add:
        // List<Resume> resumeList = resumeRepository.findAllByUserId(currentUser.getId());
        // List<ResumeResponse> resumeResponses = new ArrayList<>();
        // resumeList.forEach(
        //         (resume) -> resumeResponses.add(
        //                 ResumeResponse.builder()
        //                         .content(resume.getContent())
        //                         .fileName(resume.getFileName())
        //                         .isMain(resume.isMain())
        //                         .createdAt(resume.getCreatedAt())
        //                         .id(resume.getId())
        //                         .build()
        //         )
        // );
        // return resumeResponses;

        // fetch all resumes for this user, then convert each Resume entity
        // into a ResumeResponse DTO using stream — cleaner than a manual forEach loop
        return resumeRepository.findAllByUserId(currentUser.getId())
                .stream()
                .map(resume -> ResumeResponse.builder()
                        .id(resume.getId())
                        .fileName(resume.getFileName())
                        .content(resume.getContent())
                        .isMain(resume.isMain())
                        .createdAt(resume.getCreatedAt())
                        .build())
                .toList();
    }
    public ResumeResponse getResume(Long id, User currentUser){
        Resume resume = resumeRepository.findByIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found"));
        return ResumeResponse.builder()
                .fileName(resume.getFileName())
                .id(resume.getId())
                .content(resume.getContent())
                .isMain(resume.isMain())
                .createdAt(resume.getCreatedAt())
                .build();

    }
    public ResumeResponse uploadResume(MultipartFile file, boolean isMain, User currentUser){
        String parsedText = extractFile(file);
        Resume resume = Resume.builder()
                        .user(currentUser)
                        .content(parsedText)
                        .fileName(file.getOriginalFilename())
                        .isMain(isMain)
                        .build();

        // if the new resume is being set as main, find the current main resume for this user
        // and unset it — only one resume can be main at a time
        // save() on an existing entity does an UPDATE not an INSERT because it already has an id
        if(isMain){
            resumeRepository.findByUserIdAndIsMainTrue(currentUser.getId())
                    .ifPresent(existing -> {
                        existing.setMain(false);
                        resumeRepository.save(existing);
                    });
        }
        Resume savedResume = resumeRepository.save(resume);
        return ResumeResponse.builder()
                .id(savedResume.getId())
                .fileName(file.getOriginalFilename())
                .content(savedResume.getContent())
                .isMain(savedResume.isMain())
                .createdAt(savedResume.getCreatedAt())
                .build();

    }

    //delete a resume
    public void deleteResume(Long resumeId, User currentUser){
        //make sure the resume exists and belongs to the user
        Resume resume = resumeRepository.findByIdAndUserId(resumeId, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found"));
        resumeRepository.delete(resume);
    }

    //extract the string from the uploaded pdf
    private String extractFile(MultipartFile file){
        try{
            PDDocument document = Loader.loadPDF(file.getBytes());
            PDFTextStripper stripper = new PDFTextStripper(); //text striopper to extract text
            String text = stripper.getText(document);
            document.close();
            return text;
        }
        catch (IOException e){ //PDFBOX throws IOEXception if the file is correupted or unreadable
            throw new RuntimeException("Failed to extract from PDF");
            //we catch it and throw oour cleaner excepton
        }

    }
    public ResumeResponse setResumeMain(Long resumeId, User user){
        Resume resume = resumeRepository.findByIdAndUserId(resumeId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Resume does not exist"));
        resumeRepository.findByUserIdAndIsMainTrue(user.getId())
                .ifPresent(existing -> {
                    existing.setMain(false);
                    resumeRepository.save(existing);
                });
        resume.setMain(true);
        Resume saved = resumeRepository.save(resume);
        return ResumeResponse.builder()
                .id(saved.getId())
                .fileName(saved.getFileName())
                .content(saved.getContent())
                .isMain(saved.isMain())
                .createdAt(saved.getCreatedAt())
                .build();
    }
}
