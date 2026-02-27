package com.passwordmanager.service;

import java.util.Map;

public interface BackupService {

    String exportBackup();

    Map<String, Object> restoreBackup(String fileContent);

    Map<String, Object> updateBackup(String fileContent);

    Map<String, Object> validateBackup(String fileContent);

    String deleteBackup();

    Map<String, Object> latestBackupInfo();
}
