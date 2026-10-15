# AGENTS.md

## Projekat
Expense Splitter: dijeljenje troskova u grupi. Zahtjevi su u SPEC.md.

## Stack i struktura
- Backend: Spring Boot 4 + Java 21 + Spring Data JPA + MySQL (server/)
- Frontend: React + Vite (client/), poziva backend preko /api (Vite proxy na localhost:8080)
- Logika obracuna je u server/src/main/java/com/coinis/splitter/settlement/SettlementCalculator.java
- Baza: MySQL 8.4 u Docker kontejneru, definisan u docker-compose.yml

## Komande
- Pokretanje baze: docker compose up -d
- Instalacija frontenda: npm --prefix client install
- Testovi backenda: cd server && ./mvnw test (Windows: mvnw.cmd test)
- Testovi frontenda: npm --prefix client test
- Lint frontenda: npm --prefix client run lint
- Pokretanje backenda: cd server && ./mvnw spring-boot:run
- Pokretanje frontenda: npm --prefix client run dev

## Pravila
- Iznosi su cijeli brojevi u centima (long / number), nikad float ili double
- Parsiranje unosa "90,00" ili "90.00" radi samo u client/src/money.js
- Visak centa pri dijeljenju ide prvom ucesniku po redu unosa (id rastuce)
- Svaki scenarij iz SPEC.md ima svoj test
- Pokreni /provjeri prije nego sto kazes da je zadatak gotov i prije commita
- Jedna funkcionalnost = jedan commit, poruka u formatu tipa: "feat: ...", "fix: ...", "chore: ..."

## Ne diraj
- .env i sve tajne (lozinke za bazu su samo u docker-compose.yml za lokalni razvoj)
- server/mvnw i server/.mvn bez razloga
