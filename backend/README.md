Spring Boot backend for password manager module.

## Database (MySQL)

The app now uses MySQL instead of H2.

Set these environment variables before starting:

- `DB_HOST` (default: `localhost`)
- `DB_PORT` (default: `3306`)
- `DB_NAME` (default: `vault_db`)
- `DB_USER` (default: `root`)
- `DB_PASSWORD` (default: `root`)

Example (PowerShell):

```powershell
$env:DB_HOST="localhost"
$env:DB_PORT="3306"
$env:DB_NAME="vault_db"

$env:DB_USER="root"
$env:DB_PASSWORD="root"
mvn spring-boot:run
```
