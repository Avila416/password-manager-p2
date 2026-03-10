# Backup Operations & Vault Security Fixes

This document outlines the changes required to fix the following issues in the Password Manager application:

1. Decrypted password should only be revealed after valid master password entry
2. Before deleting any vault entry, user must first export a backup
3. Validate, Update, and Restore operations should auto-restore deleted entries from backup
4. Master password verification required for viewing and deleting vault entries

---

## Order of Implementation

### Step 1: Create BackupRequiredException

**File:** `backend/src/main/java/com/passwordmanager/exception/BackupRequiredException.java`

```java
package com.passwordmanager.exception;

public class BackupRequiredException extends RuntimeException {
    public BackupRequiredException(String msg) {
        super(msg);
    }
}
```

---

### Step 2: Update GlobalExceptionHandler

**File:** `backend/src/main/java/com/passwordmanager/exception/GlobalExceptionHandler.java`

Add the following handler method:

```java
@ExceptionHandler(BackupRequiredException.class)
public ResponseEntity<?> handleBackupRequired(BackupRequiredException ex) {
    return new ResponseEntity<>(buildResponse(ex.getMessage(), HttpStatus.PRECONDITION_FAILED), HttpStatus.PRECONDITION_FAILED);
}
```

---

### Step 3: Update BackupService Interface

**File:** `backend/src/main/java/com/passwordmanager/service/BackupService.java`

Add these new method signatures:

```java
Map<String, Object> validateAndRestoreBackup(String fileContent);

Map<String, Object> updateAndRestoreBackup(String fileContent);

boolean hasValidBackup();

String getLatestBackupContent();
```

---

### Step 4: Implement New Methods in BackupServiceImpl

**File:** `backend/src/main/java/com/passwordmanager/service/impl/BackupServiceImpl.java`

Add implementations after `latestBackupInfo()` method:

```java
@Override
public boolean hasValidBackup() {
    List<BackupFile> backups = backupFileRepository.findAll();
    if (backups.isEmpty()) {
        return false;
    }
    BackupFile latest = backups.stream().max(Comparator.comparing(BackupFile::getCreatedAt)).orElse(null);
    if (latest == null || !fileUtil.validate(latest.getEncryptedContent())) {
        return false;
    }
    // Verify the backup content is valid and decryptable
    String payload = latest.getChecksum() + "." + latest.getEncryptedContent();
    return isValidPayload(payload);
}

@Override
public String getLatestBackupContent() {
    List<BackupFile> backups = backupFileRepository.findAll();
    if (backups.isEmpty()) {
        return null;
    }
    BackupFile latest = backups.stream().max(Comparator.comparing(BackupFile::getCreatedAt)).orElse(null);
    if (latest == null || !fileUtil.validate(latest.getEncryptedContent())) {
        return null;
    }
    return latest.getChecksum() + "." + latest.getEncryptedContent();
}

@Override
@Transactional
public Map<String, Object> validateAndRestoreBackup(String fileContent) {
    // First validate the backup
    Map<String, Object> validationResult = validateBackup(fileContent);

    // Then restore the backup
    Map<String, Object> restoreResult = restoreBackup(fileContent);

    return Map.of(
            "message", "Backup validated and restored successfully",
            "checksum", validationResult.get("checksum"),
            "encryptedLength", validationResult.get("encryptedLength"),
            "payloadLength", validationResult.get("payloadLength"),
            "validatedAt", validationResult.get("validatedAt"),
            "restoredEntries", restoreResult.get("restoredEntries"),
            "restoredAt", restoreResult.get("restoredAt")
    );
}

@Override
@Transactional
public Map<String, Object> updateAndRestoreBackup(String fileContent) {
    // First update the backup
    Map<String, Object> updateResult = updateBackup(fileContent);

    // Then restore the backup
    Map<String, Object> restoreResult = restoreBackup(fileContent);

    return Map.of(
            "message", "Backup updated and restored successfully",
            "fileName", updateResult.get("fileName"),
            "filePath", updateResult.get("filePath"),
            "checksum", updateResult.get("checksum"),
            "payloadLength", updateResult.get("payloadLength"),
            "updatedAt", updateResult.get("updatedAt"),
            "restoredEntries", restoreResult.get("restoredEntries"),
            "restoredAt", restoreResult.get("restoredAt")
    );
}
```

---

### Step 5: Update VaultService

**File:** `backend/src/main/java/com/passwordmanager/service/VaultService.java`

1. Add import:
```java
import com.passwordmanager.exception.BackupRequiredException;
```

2. Add BackupService dependency:
```java
private final BackupService backupService;
```

3. Update constructor to include BackupService (handled automatically by @RequiredArgsConstructor)

4. Modify `deleteEntry()` method:
```java
@Transactional
public void deleteEntry(Long id, String masterPassword) {
    if (!masterPasswordValidator.verify(masterPassword)) {
        throw new UnauthorizedAccessException("Invalid master password");
    }
    if (!passwordEntryRepository.existsById(id)) {
        throw new InvalidInputException("Entry not found");
    }
    if (!backupService.hasValidBackup()) {
        throw new BackupRequiredException("A valid backup is required before deleting vault entries. Please export a backup first through Backup Operations.");
    }
    passwordEntryRepository.deleteById(id);
}
```

---

### Step 6: Update BackupController

**File:** `backend/src/main/java/com/passwordmanager/controller/BackupController.java`

Change the validate and update endpoints to use combined methods:

```java
@PutMapping("/update")
public Map<String, Object> updateBackup(@RequestBody JsonNode req) {
    return service.updateAndRestoreBackup(extractFileContent(req));
}

@PatchMapping("/validate")
public Map<String, Object> validateBackup(@RequestBody JsonNode req) {
    return service.validateAndRestoreBackup(extractFileContent(req));
}
```

---

### Step 7: Update Frontend Console Component TypeScript

**File:** `frontend/src/app/pages/console/console.component.ts`

1. Update `validateBackup()` method:
```typescript
validateBackup(): void {
  const normalizedContent = this.normalizeBackupContent('validate');
  if (!normalizedContent) {
    return;
  }
  this.http.patch<BackupValidateResponse & { restoredEntries?: number }>(`${this.apiBaseUrl}/backup/validate`, { fileContent: normalizedContent }).subscribe({
    next: (res) => {
      this.backupValidationResult = res;
      this.backupUpdateResult = null;
      const restoredMsg = res.restoredEntries !== undefined ? ` Restored ${res.restoredEntries} entries.` : '';
      this.showSuccess(`Backup validated and restored.${restoredMsg}`);
      this.revealedPasswords = {};
      this.fetchEntries();
      this.fetchAuditLogs();
      this.fetchDashboard();
    },
    error: (err: HttpErrorResponse) => {
      this.showError(this.extractErrorMessage(err, 'Backup validation failed.'));
    }
  });
}
```

2. Update `updateBackup()` method:
```typescript
updateBackup(): void {
  const normalizedContent = this.normalizeBackupContent('update');
  if (!normalizedContent) {
    return;
  }
  this.http.put<BackupUpdateResponse & { restoredEntries?: number }>(`${this.apiBaseUrl}/backup/update`, { fileContent: normalizedContent }).subscribe({
    next: (res) => {
      this.backupUpdateResult = res;
      this.backupValidationResult = null;
      const restoredMsg = res.restoredEntries !== undefined ? ` Restored ${res.restoredEntries} entries.` : '';
      this.showSuccess(`Backup updated and restored.${restoredMsg}`);
      this.revealedPasswords = {};
      this.fetchEntries();
      this.fetchLatestBackupInfo();
      this.fetchAuditLogs();
      this.fetchDashboard();
    },
    error: (err: HttpErrorResponse) => {
      this.showError(this.extractErrorMessage(err, 'Failed to update backup.'));
    }
  });
}
```

---

### Step 8: Update Frontend Console Component HTML

**File:** `frontend/src/app/pages/console/console.component.html`

1. Update help text:
```html
<p class="help">
  Steps: 1) Click <strong>Export</strong> to generate backup (required before deleting any vault entry).
  2) Click <strong>Validate</strong> to verify backup integrity and restore vault entries.
  3) Click <strong>Update</strong> to overwrite latest backup and restore vault entries.
  4) Click <strong>Restore</strong> to recover vault data from backup content.
  5) Click <strong>Delete</strong> to remove the latest backup file.
  <br/><em>Note: Master password is required to view or delete vault entries.</em>
</p>
```

2. Add hint in delete modal (inside modal-content div):
```html
<p *ngIf="masterAction === 'delete'" class="hint"><em>Note: A valid backup must exist before deletion. Export first if needed.</em></p>
```

---

### Step 9: Update Frontend Vault Component HTML

**File:** `frontend/src/app/vault/vault.component.html`

Add hint in delete modal:
```html
<section class="modal" *ngIf="deleteEntryTarget">
  <div class="modal-content">
    <h3>Security Verification</h3>
    <p>Please enter your master password to delete this entry.</p>
    <p class="hint"><em>Note: A valid backup must exist before deletion. Export through Backup Operations first.</em></p>
    <!-- rest of modal content -->
  </div>
</section>
```

---

### Step 10: Add CSS Styling for Hint Class

**File:** `frontend/src/app/pages/console/console.component.css`

Add:
```css
.hint {
  color: #6a7a82;
  font-size: 0.9rem;
  margin: 0.5rem 0;
}
```

**File:** `frontend/src/app/vault/vault.component.css`

Add:
```css
.hint {
  color: #6a7a82;
  font-size: 0.9rem;
  margin: 0.5rem 0;
}
```

---

## Testing the Changes

1. **Test Backup Requirement for Delete:**
   - Try to delete a vault entry without any backup → Should fail with "backup required" error
   - Export a backup first, then try to delete → Should succeed

2. **Test Validate with Auto-Restore:**
   - Create entries A, B, C
   - Export backup
   - Delete entry C
   - Click Validate with the backup content → Entry C should be restored

3. **Test Update with Auto-Restore:**
   - Same as above but using Update button

4. **Test Restore:**
   - Same as above but using Restore button

5. **Test Master Password Verification:**
   - Click "View Password" on any entry → Should prompt for master password
   - Enter wrong password → Should fail
   - Enter correct password → Should reveal decrypted password

---

## File Changes Summary

| File | Type | Changes |
|------|------|---------|
| `BackupRequiredException.java` | New | Exception class |
| `GlobalExceptionHandler.java` | Modified | Added exception handler |
| `BackupService.java` | Modified | Added 4 new method signatures |
| `BackupServiceImpl.java` | Modified | Implemented 4 new methods |
| `VaultService.java` | Modified | Added backup check in deleteEntry |
| `BackupController.java` | Modified | Changed validate/update to auto-restore |
| `console.component.ts` | Modified | Updated validate/update methods |
| `console.component.html` | Modified | Updated help text and modal |
| `vault.component.html` | Modified | Added hint in delete modal |
| `console.component.css` | Modified | Added .hint class |
| `vault.component.css` | Modified | Added .hint class |
