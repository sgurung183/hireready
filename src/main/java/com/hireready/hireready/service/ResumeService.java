package com.hireready.hireready.service;

import com.hireready.hireready.dto.response.ResumeResponse;
import com.hireready.hireready.entity.Resume;
import com.hireready.hireready.entity.User;
import com.hireready.hireready.repository.ResumeRepository;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class ResumeService {
    private final ResumeRepository resumeRepository;

    public ResumeResponse getResume(Long id, User currentUser){
        Resume resume = resumeRepository.findByIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Resume not Found!!!"));
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
        Resume savedResume = resumeRepository.save(resume);
        return ResumeResponse.builder()
                .id(savedResume.getId())
                .fileName(file.getOriginalFilename())
                .content(savedResume.getContent())
                .isMain(savedResume.isMain())
                .createdAt(savedResume.getCreatedAt())
                .build();

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
}
