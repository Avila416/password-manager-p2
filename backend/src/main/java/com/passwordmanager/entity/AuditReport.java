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
public class AuditReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int totalPasswords;
    private int weakPasswords;
    private int reusedPasswords;
    private int oldPasswords;

    private LocalDateTime generatedAt;
}
