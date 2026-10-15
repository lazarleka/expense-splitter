# Expense Splitter: specifikacija

## Zahtjevi
1. Korisnik dodaje ucesnike u grupu; ime je obavezno i jedinstveno.
2. Korisnik unosi trosak: naziv, iznos, ko je platio. Trosak se dijeli jednako na sve ucesnike.
   Iznosi se cuvaju kao cijeli brojevi u centima.
3. Aplikacija prikazuje ko kome duguje (settlement kalkulacija) i minimalni UI sa formom i prikazom dugovanja.

## Pravila obracuna
- Udio = iznos / broj ucesnika (cjelobrojno dijeljenje u centima).
- Visak centa (ostatak pri dijeljenju) ide prvom ucesniku po redu unosa.
- Stanje ucesnika = (sta je platio) - (njegov udio u svim troskovima).
  Pozitivno stanje: potrazuje; negativno: duguje.
- Dugovanja: duznici se uparuju sa povjeriocima po redu unosa, dok svi stanja nisu nula.

## Scenariji
### Rucak i taksi
- Ucesnici: Ana, Marko, Iva (tim redom)
- Ana placa rucak 90,00; Marko placa taksi 30,00
- Rezultat: Iva duguje Ani 40,00; Marko duguje Ani 10,00

### Zaokruzivanje
- Ucesnici: Ana, Marko, Iva
- Ana placa 100,00 za troje
- Udjeli: 33,34 (Ana) + 33,33 (Marko) + 33,33 (Iva); zbir je tacno 100,00
- Rezultat: Marko duguje Ani 33,33; Iva duguje Ani 33,33

### Validacija unosa
- Prazan naziv ucesnika ili duplikat imena: unos se ne prihvata.
- Prazan naziv troska ili iznos <= 0: trosak se ne dodaje.

## Sta nije obuhvaeno
- Vise valuta, nejednaka podjela, prijava korisnika, vise grupa
- Brisanje i izmjena troskova i ucesnika
