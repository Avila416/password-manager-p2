# ERD Diagram

```mermaid
erDiagram
    VAULT_ENTRY {
        bigint id PK
        string title
        string username
        string password
        datetime created_at
    }

    AUDIT_LOG {
        bigint id PK
        string action
        string ip_address
        string status
        datetime timestamp
    }

    BACKUP_FILE {
        bigint id PK
        string file_name
        string encrypted_content
        string checksum
        datetime created_at
    }
```
