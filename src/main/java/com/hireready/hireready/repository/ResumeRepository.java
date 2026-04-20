package com.hireready.hireready.repository;

import com.hireready.hireready.entity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResumeRepository extends JpaRepository<Resume, Long> {
    List<Resume> findAllByUserId(Long userID);

    //find the main résumé for a specific user id
    Optional<Resume> findByUserIdAndIsMain(Long userId);

    Optional<Resume> findByFileNameIgnoreCase(String name);
}
