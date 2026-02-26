# Dashboard, Backup & Audit Monitoring Module

This Spring Boot module implements:
- Dashboard vault summary metrics
- Encrypted backup export/restore with integrity validation
- Sensitive-operation audit monitoring with traceable IP/timestamp/action
- Integration tests for core module behavior

## Implemented Features

### Dashboard
- `GET /api/dashboard`
- Returns:
  - `totalPasswords`
  - `weakPasswords`
  - `recentPasswords` (last 7 days)
  - `actionStats` map for lightweight graph/stat support

### Backup & Restore
- `GET /api/backup/export`
  - Exports encrypted backup payload
  - Persists backup metadata in DB
  - Logs `BACKUP_EXPORT`
- `PATCH /api/backup/validate`
  - Validates backup payload format and checksum integrity
- `POST /api/backup/restore`
  - Validates then decrypts payload
- `PUT /api/backup/update`
  - Updates latest backup record with new encrypted payload
- `DELETE /api/backup/delete`
  - Deletes latest backup record

Backup payload format:
- `<sha256(encryptedData)>.<encryptedData>`

### Logging & Monitoring
- Audit log entity captures:
  - Action type
  - Simulated IP
  - Status
  - Timestamp
- Sensitive operations logged:
  - `LOGIN_ATTEMPT`
  - `FAILED_LOGIN_ATTEMPT`
  - `MASTER_PASSWORD_CHANGE`
  - `PASSWORD_VIEW`
  - `DELETE_ENTRY`
  - `BACKUP_EXPORT`
- Audit view endpoint:
  - `GET /api/audit`
  - Optional filters: `action`, `status`, `ip`

### Monitoring/Vault Endpoints
- `POST /api/vault/monitor/login?success=true|false&ip=...`
- `POST /api/vault/monitor/master-password-change?ip=...`
- `GET /api/vault/entries/{id}?ip=...` (logs password view)
- `DELETE /api/vault/entries/{id}?ip=...` (logs delete entry)
- `GET /api/vault/entries`
- `POST /api/vault/entries`

## Integration Testing
- Integration test class:
  - `src/test/java/com/passwordmanager/integration/ModuleIntegrationTest.java`
- Covers:
  - Dashboard summary counts
  - Backup export/validate/restore flow
  - Sensitive operations audit trail

## Documentation Artifacts
- ERD: `docs/ERD.md`
- Architecture diagram: `docs/ARCHITECTURE.md`

## Run

```bash
mvn clean test
```

## MySQL Configuration

The backend now uses MySQL (not H2). Default datasource values:

- `DB_URL=jdbc:mysql://localhost:3306/vaultdb?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC`
- `DB_USERNAME=root`
- `DB_PASSWORD=root`

You can override these with environment variables before running the app.
