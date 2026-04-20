package com.hireready.hireready.repository;

import com.hireready.hireready.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
//JPARepository<User,Long> - User tells JPA which entity the repo is managing
//Long is the type of the primary key
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
