---
name: provjeri
description: Pokree testove i lint i sazima rezultat.
---
Koristi prije nego sto kazes da je zadatak gotov.

1. Pokreni backend testove: `cd server && ./mvnw test` (Windows: `mvnw.cmd test`).
2. Pokreni frontend testove i lint: `npm --prefix client test` i `npm --prefix client run lint`.
3. Prikazi koliko testova prolazi i koji padaju.
4. Ako nesto pada, objasni uzrok. Ne mijenjaj testove da bi prosli.
