# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

@AGENTS.md

## Pojedinacni testovi
- Backend, jedna metoda: `cd server && ./mvnw test -Dtest=SettlementCalculatorTest#lunchAndTaxi` (Windows: `mvnw.cmd`)
- Frontend, jedan fajl ili naziv: `npm --prefix client test -- money.test.js` ili `cd client && npx vitest run -t "parsira"`

## Arhitektura (velika slika)
- **Novac:** svi iznosi su `long` centi na serveru i `number` centi u klijentu. Jedino mjesto gdje se tekst pretvara u cente je `client/src/money.js` (`parseAmountToCents`). Ne uvoditi float.
- **Obracun je cista logika:** `server/.../settlement/SettlementCalculator` nema Spring ni JPA zavisnosti. `shares()` dijeli iznos i daje visak centa prvim ucesnicima, a `settle()` uparuje duznike i povjerioce dvostrukim pokazivacem. Redoslijed ucesnika je id rastuce (`findAllByOrderByIdAsc`), pa su zaokruzivanje i redoslijed dugovanja deterministicni. Ako se promijeni redoslijed upita, mijenja se i rezultat.
- **Sloj API-ja:** kontroleri u `server/.../api` rade direktno sa repozitorijima, bez servisnog sloja. `SettlementController` na svaki GET ucitava sve ucesnike i sve troskove i pretvara troskove u `PaidAmount`. Nema kesiranja.
- **Greske:** validacija ide preko `jakarta.validation` anotacija u DTO-ima (400). Ostale greske su `ResponseStatusException` (400 nepoznat platilac, 409 duplikat imena). Spring ne vraca tekst poruke, pa klijent mapira statusne kodove u poruke u `ERROR_MESSAGES` u `App.jsx`.
- **Perzistencija:** JPA entiteti sa `IDENTITY` id-jevima. Sema se pravi sa `ddl-auto=update`, nema migracija. Obrisani redovi ostavljaju rupe u id-jevima.
- **Klijent:** cijela aplikacija je u jednoj komponenti `App.jsx`. Stanje je u useState-ovima. Svaka izmjena zove `reload()`, koji ponovo ucitava sva tri endpoint-a (`/participants`, `/expenses`, `/settlements`). Vite proxy salje `/api` na `localhost:8080`.

## Zamke pri radu
- Baza je na portu `3306` po defaultu. Ako ga zauzima drugi MySQL, backend se pokrece sa `DB_PORT=<port>`.
- Ako Maven pada na SSL gresci (korporativni proxy), koristiti `MAVEN_OPTS="-Djavax.net.ssl.trustStoreType=Windows-ROOT"`. Ne dodavati to u projekat.
- Backend nema testove koji podizu Spring kontekst ili bazu, pa `./mvnw test` radi bez MySQL-a. Kontroleri i baza se provjeravaju rucno ili kroz pokrenuti backend.
