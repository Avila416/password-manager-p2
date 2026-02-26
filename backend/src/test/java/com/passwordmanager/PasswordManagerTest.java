package com.passwordmanager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.passwordmanager.entity.AuditLog;
import com.passwordmanager.entity.VaultEntry;
import com.passwordmanager.repository.AuditLogRepository;
import com.passwordmanager.repository.BackupFileRepository;
import com.passwordmanager.repository.VaultEntryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PasswordManagerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private VaultEntryRepository vaultEntryRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private BackupFileRepository backupFileRepository;

    @BeforeEach
    void setup() {
        auditLogRepository.deleteAll();
        backupFileRepository.deleteAll();
        vaultEntryRepository.deleteAll();

        vaultEntryRepository.save(new VaultEntry(null, "Email", "demo", "Strong#123", LocalDateTime.now().minusDays(1)));
        vaultEntryRepository.save(new VaultEntry(null, "Legacy", "old-user", "weak", LocalDateTime.now().minusDays(1)));
        vaultEntryRepository.save(new VaultEntry(null, "Archive", "arch", "Good#Pass9", LocalDateTime.now().minusDays(30)));
    }

    @Test
    void dashboardShouldReturnRealSummaryCounts() throws Exception {
        mockMvc.perform(get("/api/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPasswords").value(3))
                .andExpect(jsonPath("$.weakPasswords").value(1))
                .andExpect(jsonPath("$.recentPasswords").value(2));
    }

    @Test
    void backupExportValidateAndRestoreShouldWork() throws Exception {
        String payload = mockMvc.perform(get("/api/backup/export"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String req = objectMapper.writeValueAsString(new FileBody(payload));
        mockMvc.perform(patch("/api/backup/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(req))
                .andExpect(status().isOk());

        String decrypted = mockMvc.perform(post("/api/backup/restore")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(req))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(decrypted).contains("vault-data-");
    }

    @Test
    void sensitiveOperationsShouldBeAudited() throws Exception {
        VaultEntry entry = vaultEntryRepository.save(
                new VaultEntry(null, "Social", "social-user", "Strong#777", LocalDateTime.now())
        );

        mockMvc.perform(post("/api/vault/monitor/login")
                        .param("success", "true")
                        .param("ip", "10.0.0.1"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/vault/monitor/login")
                        .param("success", "false")
                        .param("ip", "10.0.0.2"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/vault/monitor/master-password-change")
                        .param("ip", "10.0.0.3"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/vault/entries/" + entry.getId())
                        .param("ip", "10.0.0.4"))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/vault/entries/" + entry.getId())
                        .param("ip", "10.0.0.5"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/backup/export"))
                .andExpect(status().isOk());

        List<AuditLog> logs = auditLogRepository.findAll();
        assertThat(logs).extracting(AuditLog::getAction).contains(
                "LOGIN_ATTEMPT",
                "FAILED_LOGIN_ATTEMPT",
                "MASTER_PASSWORD_CHANGE",
                "PASSWORD_VIEW",
                "DELETE_ENTRY",
                "BACKUP_EXPORT"
        );
    }

    @Test
    void auditFiltersShouldSupportCaseInsensitiveContainsMatching() throws Exception {
        mockMvc.perform(delete("/api/vault/entries/999999")
                        .param("ip", "127.0.0.1"))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/audit")
                        .param("action", "delete")
                        .param("status", "fail")
                        .param("ip", "127.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].action").value("DELETE_ENTRY"))
                .andExpect(jsonPath("$[0].status").value("FAILED"))
                .andExpect(jsonPath("$[0].ip").value("127.0.0.1"));
    }

    static class FileBody {
        private String fileContent;

        public FileBody(String fileContent) {
            this.fileContent = fileContent;
        }

        public String getFileContent() {
            return fileContent;
        }

        public void setFileContent(String fileContent) {
            this.fileContent = fileContent;
        }
    }
}
