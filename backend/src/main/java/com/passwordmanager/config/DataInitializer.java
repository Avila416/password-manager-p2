package com.passwordmanager.config;

import com.passwordmanager.entity.VaultEntry;
import com.passwordmanager.repository.VaultEntryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private final VaultEntryRepository vaultEntryRepository;

    public DataInitializer(VaultEntryRepository vaultEntryRepository) {
        this.vaultEntryRepository = vaultEntryRepository;
    }

    @Override
    public void run(String... args) {
        if (vaultEntryRepository.count() > 0) {
            return;
        }

        vaultEntryRepository.save(new VaultEntry(null, "Email", "user@example.com", "Str0ng@Pass1", LocalDateTime.now().minusDays(1)));
        vaultEntryRepository.save(new VaultEntry(null, "Bank", "user.bank", "weak", LocalDateTime.now().minusDays(2)));
        vaultEntryRepository.save(new VaultEntry(null, "Work", "employee01", "An0ther#Pass", LocalDateTime.now().minusDays(10)));
    }
}
