package com.passwordmanager.entity;


import lombok.extern.slf4j.Slf4j;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@Slf4j
public class SecurityAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String message;
    private String severity; // LOW, MEDIUM, HIGH
    private String type; // WEAK, REUSED, OLD

    private LocalDateTime createdAt;
}
