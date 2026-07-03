package com.passwordmanager.entity;


import lombok.extern.slf4j.Slf4j;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Slf4j
public class PasswordEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String username;

    // for audit module we just store plain for now
    private String encryptedPassword;

    private String website;

    private String category;

    private Boolean favorite;

    private LocalDateTime createdAt;
}

