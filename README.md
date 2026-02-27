# Password Manager (P2)

Full-stack monolithic password manager with Spring Boot backend and Angular frontend.

## Overview
This application helps users securely manage account credentials with:
- account authentication
- master-password based vault access
- password generation and strength scoring
- security audit insights
- encrypted backup operations

## Tech Stack
- Backend: Java 17, Spring Boot, Spring Security, Spring Data JPA, MySQL
- Frontend: Angular
- Build tools: Maven, npm

## Repository Structure
- `backend/` - Spring Boot API and business logic
- `frontend/` - Angular web app
- `backend/docs/ERD.md` - entity relationship documentation
- `backend/docs/ARCHITECTURE.md` - architecture notes

## Core Modules
- Authentication & account (`/auth`)
- Vault management (`/api/vault`)
- Password generator + security audit (`/api/generator`)
- Dashboard summary (`/api/dashboard`)
- Backup operations (`/api/backup`)
- Audit logs (`/api/audit`)

## Prerequisites
- Java 17+
- Maven 3.9+
- Node.js 18+ and npm
- MySQL 8+

## Configuration
Backend config: `backend/src/main/resources/application.properties`

Important defaults:
- Backend port: `8084`
- Frontend dev port: `4200`
- DB URL: `jdbc:mysql://localhost:3306/vault_db`
- DB user: `root`

Update DB credentials before running in your environment.

## Run Locally

### 1) Start Backend
```bash
cd backend
mvn spring-boot:run
```
Backend base URL: `http://localhost:8084`

### 2) Start Frontend
```bash
cd frontend
npm install
npm start
```
Frontend URL: `http://localhost:4200`

## Build

### Backend
```bash
cd backend
mvn -DskipTests compile
```

### Frontend
```bash
cd frontend
npm run build
```

## Implemented Features (Current)
- Register/login/logout
- Master password setup/change/reset
- 2FA enable/disable with OTP simulation
- Vault CRUD, favorites, search/filter/sort
- Verify/reveal password using master password
- Password generator with configurable options
- Password strength evaluation
- Security audit (weak/reused/old analysis + alerts)
- Encrypted backup export/validate/update/restore/delete
- Dashboard and audit log views

## Known Gaps / Planned
- Security questions workflow
- Profile edit screen/API (name/email/phone update)
- Notes field for vault entries
- Native file-upload based backup import UX

## API Examples

### Register
`POST /auth/register`
```json
{
  "name": "User",
  "email": "user@example.com",
  "password": "Password@123",
  "phone": "9876543210"
}
```

### Add Vault Entry
`POST /api/vault`
```json
{
  "title": "Github",
  "username": "user@example.com",
  "password": "MySecret@123",
  "website": "github.com",
  "category": "WORK"
}
```

### Generate Password
`POST /api/generator/generate`
```json
{
  "length": 16,
  "uppercase": true,
  "lowercase": true,
  "numbers": true,
  "specialChars": true,
  "excludeSimilar": true,
  "count": 5
}
```

## Security Notes
- Vault passwords are stored encrypted in DB.
- Sensitive vault actions require master-password verification in UI/API flow.
- OTP/verification codes are simulated for development.
- Do not use development secrets in production.

## Testing
Current repository includes compile/build checks and basic test scaffolding under `backend/src/test`.
Add module-level unit/integration tests for production readiness.
