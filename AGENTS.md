# AGENTS.md

## Projekat
Expense Splitter: korisnici se prijavljuju, dodaju prijatelje, prave dogadjaje, pozivaju prijatelje i dijele troskove.
Zahtjevi su u SPEC.md.

## Stack i struktura
- Backend: Spring Boot 4 + Java 21 + Spring Data JPA + Spring Security + MySQL (server/)
- Frontend: React + Vite + react-router (client/), poziva backend preko /api (Vite proxy na localhost:8080)
- Logika obracuna je u server/src/main/java/com/coinis/splitter/settlement/SettlementCalculator.java
  i ostaje bez Spring i JPA zavisnosti
- Baza: MySQL 8.4 u Docker kontejneru, definisan u docker-compose.yml

## Komande
- Pokretanje baze: docker compose up -d
- Reset baze (brise sve podatke): docker compose down -v pa docker compose up -d
- Instalacija frontenda: npm --prefix client install
- Testovi backenda: cd server && ./mvnw test (Windows: mvnw.cmd test)
- Testovi frontenda: npm --prefix client test
- Lint frontenda: npm --prefix client run lint
- Pokretanje backenda: cd server && ./mvnw spring-boot:run
- Pokretanje frontenda: npm --prefix client run dev

## Pravila
- Iznosi su cijeli brojevi u centima (long / number), nikad float ili double
- Parsiranje unosa "90,00" ili "90.00" radi samo u client/src/money.js
- Visak centa ide prvim clanovima po redu ulaska u dogadjaj (kreator prvi)
- Svaki endpoint dogadjaja provjerava da je prijavljeni korisnik clan; ako nije, vraca 404
- Korisnik se uvijek uzima iz sesije, nikad iz tijela ili parametra zahtjeva
- Lozinke se hashuju (BCrypt) i nikad se ne vracaju u odgovoru niti ispisuju u log
- Arhiviran dogadjaj odbija svaku izmjenu (409)
- U dogadjaj se poziva samo prijatelj kreatora; prijateljstvo je samo prihvacen zahtjev, u bilo kom smjeru
- Avatar je kljuc iz fiksnog skupa (enum Avatar na serveru); klijent ga prikazuje preko client/src/avatars.js
- Svaki scenarij iz SPEC.md ima svoj test
- Pokreni /provjeri prije nego sto kazes da je zadatak gotov i prije commita
- Jedna funkcionalnost = jedan commit, poruka u formatu tipa: "feat: ...", "fix: ...", "chore: ..."

## Ne diraj
- .env i sve tajne (lozinke za bazu su samo u docker-compose.yml za lokalni razvoj)
- server/mvnw i server/.mvn bez razloga
