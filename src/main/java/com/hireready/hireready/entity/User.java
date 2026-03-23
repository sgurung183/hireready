package com.hireready.hireready.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.Id;

import java.time.LocalDateTime;

@Entity //tells JPA this class maps to a database table
@Table(name = "users") //name the table
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    //mark the primary key and tell JPA to auto increment
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)//field is required in the database
    private String fullName;

    @Column(nullable = false, unique = true)//field needs to be unique
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)//stores the enum as a text in the database rather than a number
    @Column(nullable = false)
    private VisaStatus visaStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist //runs onCreate() method automaticallt right before a new record i saved
    void onCreate(){
        createdAt = LocalDateTime.now();
    }
}
