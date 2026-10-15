# Expense Splitter: specifikacija

## Zahtjevi

### 1. Korisnici i prijava
- Korisnik se registruje korisnickim imenom i lozinkom. Korisnicko ime je obavezno i jedinstveno.
- Lozinka ima najmanje 8 znakova i cuva se samo kao hash.
- Prijava i odjava rade preko sesije (cookie). Bez prijave korisnik vidi samo ekran za prijavu i registraciju.

### 2. Prijatelji
- Korisnik salje zahtjev za prijateljstvo drugom korisniku po korisnickom imenu.
- Primalac prihvata ili odbija zahtjev. Tek kad prihvati, njih dvoje su prijatelji (obostrano).
- Odbijeni zahtjev moze se poslati ponovo.
- Ne moze se poslati zahtjev sebi, nepostojecem korisniku, postojecem prijatelju
  ni kad izmedju njih vec postoji zahtjev na cekanju (u bilo kom smjeru).
- Korisnik vidi listu prijatelja, primljene zahtjeve na cekanju i poslate zahtjeve na cekanju.

### 3. Dogadjaji
- Posle prijave korisnik vidi tri liste: pozivnice na cekanju, aktivni dogadjaji i arhivirani dogadjaji.
- Korisnik kreira dogadjaj sa nazivom (obavezan). Kreator automatski postaje prvi clan.
- Korisnik vidi samo dogadjaje u kojima je clan.

### 4. Pozivnice
- Kreator dogadjaja poziva nove ucesnike samo sa svoje liste prijatelja.
- Pozvani korisnik prihvata ili odbija pozivnicu. Tek kad prihvati postaje clan.
- Odbijeni korisnik moze ponovo biti pozvan.
- Ne moze se pozvati korisnik koji nije prijatelj kreatora, postojeci clan ni korisnik koji vec ima pozivnicu na cekanju.

### 5. Troskovi
- Clan unosi trosak: naziv, iznos i ko je platio (platilac mora biti clan).
- Iznosi se cuvaju kao cijeli brojevi u centima.
- Trosak se dijeli jednako na clanove koji su clanovi u trenutku unosa troska.
  Clan koji se pridruzi kasnije ne ucestvuje u ranijim troskovima.

### 6. Obracun (po dogadjaju)
- Udio = iznos / broj clanova troska (cjelobrojno dijeljenje u centima).
- Visak centa ide prvim clanovima po redu ulaska u dogadjaj (kreator prvi, pa ostali po vremenu prihvatanja).
- Stanje clana = (sta je platio) - (zbir njegovih udjela).
  Pozitivno stanje: potrazuje; negativno: duguje.
- Dugovanja: duznici se uparuju sa povjeriocima po redu ulaska, dok sva stanja nisu nula.

### 7. Arhiviranje
- Svaki clan moze dati ili povuci saglasnost za arhiviranje.
- Dogadjaj se arhivira automatski kad su svi clanovi saglasni.
- Novi trosak ili novi clan ponistava sve date saglasnosti.
- Arhiviran dogadjaj je samo za citanje: nema novih troskova, pozivnica ni saglasnosti.

### 8. Prikaz dogadjaja
- Clanovi sjede za okruglim stolom, ravnomjerno rasporedjeni po krugu, po redu ulaska.
- Svako mjesto prikazuje ime clana i njegovo stanje.
- U sredini stola su ukupan trosak dogadjaja i dugme "+" za unos novog troska.
- Ispod stola je lista dugovanja (ko kome duguje).

## Scenariji

### Rucak i taksi
- Dogadjaj sa clanovima Ana, Marko, Iva (tim redom ulaska)
- Ana placa rucak 90,00; Marko placa taksi 30,00
- Rezultat: Iva duguje Ani 40,00; Marko duguje Ani 10,00; ukupan trosak 120,00

### Zaokruzivanje
- Dogadjaj sa clanovima Ana, Marko, Iva
- Ana placa 100,00
- Udjeli: 33,34 (Ana) + 33,33 (Marko) + 33,33 (Iva); zbir je tacno 100,00
- Rezultat: Marko duguje Ani 33,33; Iva duguje Ani 33,33

### Kasniji clan
- Ana i Marko su clanovi; Ana placa 60,00
- Iva prihvata pozivnicu; Marko placa 30,00
- Stanja: Ana +20,00; Marko -10,00; Iva -10,00
- Rezultat: Marko duguje Ani 10,00; Iva duguje Ani 10,00

### Prijatelji
- Ana salje zahtjev Marku i Ivi; Marko prihvata, Iva odbija
- Rezultat: Ana i Marko su prijatelji (oboje se vide na listi); Iva nije Anin prijatelj
- Ana ponovo salje zahtjev Ivi: zahtjev je prihvacen kao novi
- Marko salje zahtjev Ani: odbijeno, vec su prijatelji
- Ana salje zahtjev Ivi dok joj Iva vec ima zahtjev na cekanju: odbijeno

### Pozivnica
- Marko i Iva su Anini prijatelji
- Ana kreira dogadjaj i poziva Marka i Ivu
- Marko prihvata, Iva odbija
- Rezultat: clanovi su Ana i Marko; Iva ne vidi dogadjaj
- Ana poziva Petra, koji joj nije prijatelj: poziv se ne prihvata

### Arhiviranje
- Ana i Marko su clanovi; oboje daju saglasnost: dogadjaj je arhiviran
- Ako Ana da saglasnost, pa Marko unese trosak: Anina saglasnost se ponistava

### Pristup
- Korisnik koji nije clan ne vidi dogadjaj, njegove troskove ni obracun
- Bez prijave nijedan endpoint osim registracije i prijave nije dostupan

### Validacija unosa
- Prazno korisnicko ime, duplikat imena ili kratka lozinka: registracija se ne prihvata
- Prazan naziv dogadjaja: dogadjaj se ne kreira
- Prazan naziv troska, iznos <= 0 ili platilac koji nije clan: trosak se ne dodaje

## Sta nije obuhvaceno
- Vise valuta, nejednaka podjela
- Brisanje i izmjena troskova, dogadjaja i naloga
- Izlazak iz dogadjaja i izbacivanje clana
- Promjena lozinke, oporavak naloga, prijava preko drugih servisa
- Uklanjanje prijatelja
- Avatari i slike korisnika
