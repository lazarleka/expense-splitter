# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

@AGENTS.md

## Pokretanje (redoslijed)
- Baza: `docker compose up -d` (ili lokalni MySQL; drugi port se daje sa `DB_PORT=<port>`)
- Backend (8080): `cd server && ./mvnw spring-boot:run` (Windows: `mvnw.cmd`)
- Frontend (5173): `npm --prefix client run dev` ili iz korijena `npm run dev`; build: `npm --prefix client run build`
- Korijenski `package.json` ima `dev`, `build`, `test` i `lint`, koji samo prosljedjuju klijentu.

## Pojedinacni testovi
- Backend, jedna metoda: `cd server && ./mvnw test -Dtest=SettlementCalculatorTest#lunchAndTaxi` (Windows: `mvnw.cmd`)
- Backend, jedna klasa: `-Dtest=EventServiceTest`
- Frontend, jedan fajl ili naziv: `npm --prefix client test -- tableLayout.test.js` ili `cd client && npx vitest run -t "parsira"`

## Arhitektura (velika slika)
- **Novac:** svi iznosi su `long` centi na serveru i `number` centi u klijentu. Jedino mjesto gdje se tekst pretvara u cente je `client/src/money.js` (`parseAmountToCents`). Ne uvoditi float.
- **Red ulaska = id clanstva.** `EventMember` red nastaje tek kad korisnik postane clan (kreator pri kreiranju, ostali pri prihvatanju pozivnice), pa `findAllByEventIdOrderByIdAsc` daje red ulaska. Od tog reda zavise zaokruzivanje (visak centa prvima), redoslijed dugovanja i mjesta za stolom. Zato je pozivnica posebna tabela (`Invitation`), a ne status na clanstvu.
- **Clanovi troska:** `Expense.memberCount` je broj clanova u trenutku unosa; trosak dijeli prvih `memberCount` clanova po redu ulaska. Ovo radi samo zato sto clan ne moze izaci iz dogadjaja; izlazak bi trazio listu clanova po trosku.
- **Obracun je cista logika:** `settlement/SettlementCalculator` nema Spring ni JPA zavisnosti. `balances()` daje stanje po clanu, `calculate()` dodaje uparivanje duznika i povjerilaca dvostrukim pokazivacem.
- **Slojevi:** kontroleri (`api/`) -> servisi (`service/`) -> repozitoriji. Pravila (clanstvo, pozivnice, arhiviranje, ponistavanje saglasnosti) su u `EventService` i `InvitationService`. `EventService.overview()` na svaki GET ucitava clanove i troskove i racuna obracun; nema kesiranja.
- **Prijava:** Spring Security sa sesijom (`security/SecurityConfig`). Kontroleri dobijaju `AuthUser` preko `@AuthenticationPrincipal`. Prijava i registracija su u `AuthController`, odjava je filter na `POST /api/auth/logout`. CSRF je u SPA modu: klijent cita cookie `XSRF-TOKEN` i salje header `X-XSRF-TOKEN`; ako cookie nema, `api.js` ga dobija sa `GET /api/auth/csrf`.
- **Greske:** validacija preko `jakarta.validation` u DTO-ima (400). Ostalo je `ResponseStatusException`: 400 platilac nije clan, 401 neprijavljen, 403 nije kreator, 404 nepostojeci ili tudji dogadjaj (namjerno isto), 409 duplikat korisnika, pozivnica vec postoji, arhiviran dogadjaj. Spring ne vraca tekst poruke, pa klijent daje poruke po pozivu (`errors` u `api()`), jer isti kod znaci razlicito po ekranu.
- **Perzistencija:** JPA entiteti sa `IDENTITY` id-jevima. Sema se pravi sa `ddl-auto=update`, nema migracija.
- **Klijent:** `react-router` sa tri ekrana u `client/src/pages` (prijava, lista dogadjaja, dogadjaj). Svi pozivi idu kroz `client/src/api.js`. Stanje je u `useState`; posle izmjene ekran ponovo ucitava svoje podatke. Okrugli sto je `components/RoundTable.jsx`, a pozicije mjesta racuna `tableLayout.js` (cista funkcija sa testom).

## Testovi bez baze
- Servisi se testiraju Mockitom (`EventServiceTest`, `InvitationServiceTest`), kontroleri i security sa `@WebMvcTest` + `@Import(SecurityConfig.class)` + `@MockitoBean`.
- `TestData` (test paket) pravi entitete sa id-jevima preko refleksije, jer id inace dodjeljuje baza.
- Komentar `// Scenarij "..."` iznad testa oznacava koji scenarij iz SPEC.md test pokriva.

## Zamke pri radu
- Promjena entiteta ne migrira stare podatke. Posle promjene seme: `docker compose down -v` pa `docker compose up -d`.
- Ako se promijeni redoslijed upita za clanove, mijenja se rezultat obracuna i raspored za stolom.
- U `SecurityConfig` je `DispatcherType.ERROR` dozvoljen; bez toga greske iz javnih endpointa (npr. 409 pri registraciji) postaju 401. `@WebMvcTest` to ne hvata, jer MockMvc ne radi error dispatch.
- Ako Maven pada na SSL gresci (korporativni proxy), koristiti `MAVEN_OPTS="-Djavax.net.ssl.trustStoreType=Windows-ROOT"`. Ne dodavati to u projekat.
- Claude Code: `.claude/settings.json` dozvoljava testove i lint bez pitanja i zabranjuje citanje `.env` fajlova; `/provjeri` je u `.claude/skills/provjeri`.
