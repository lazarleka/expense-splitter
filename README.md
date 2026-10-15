# Expense Splitter

Web aplikacija za dijeljenje troskova u grupi. Zahtjevi i scenariji su u [SPEC.md](SPEC.md).

- **Backend:** Spring Boot 4, Java 21, Spring Data JPA (`server/`)
- **Frontend:** React + Vite (`client/`)
- **Baza:** MySQL 8.4 (`docker-compose.yml`)

## Pokretanje

1. Baza: `docker compose up -d` (ili lokalni MySQL sa bazom `expense_splitter` i korisnikom `splitter`/`splitter`).
   Ako baza ne slusa na portu 3306, backend pokrenite sa `DB_PORT` (npr. `DB_PORT=3307`).
2. Backend (port 8080): `cd server` pa `./mvnw spring-boot:run` (Windows: `mvnw.cmd spring-boot:run`).
3. Frontend (port 5173): `npm --prefix client install` pa `npm --prefix client run dev`.

Otvorite http://localhost:5173. Zahtjevi `/api` se proksiraju na backend.

## Testovi

- Backend: `cd server` pa `./mvnw test`
- Frontend: `npm --prefix client test`
- Lint: `npm --prefix client run lint`

Pravila za rad sa Claude Code-om su u [AGENTS.md](AGENTS.md), a komanda `/provjeri` u `.claude/skills/provjeri/SKILL.md`.
