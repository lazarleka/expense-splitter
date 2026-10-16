// Raspored mjesta za okruglim stolom. Clanovi dolaze po redu ulaska: prvi (kreator) sjedi gore,
// ostali redom u smjeru kazaljke na satu, ravnomjerno po krugu.
// Ovo je raspored na ekranu (procenti), ne novac, pa float ovdje nije problem.

const round = (value) => Math.round(value * 100) / 100

/**
 * @param count broj clanova
 * @param radius poluprecnik kruga mjesta u procentima sirine kontejnera
 * @returns [{ left, top }] u procentima, centar mjesta
 */
/**
 * Smjer mjesta u stepenima (-90 = gore, 0 = desno, 90 = dole), isti redoslijed kao seatPositions.
 * Koristi se da se stolica i tanjir okrenu prema centru stola.
 */
export function seatAngle(index, count) {
  return round(-90 + (360 * index) / count)
}

export function seatPositions(count, radius = 42) {
  return Array.from({ length: count }, (_, i) => {
    const angle = -Math.PI / 2 + (2 * Math.PI * i) / count
    return {
      left: round(50 + radius * Math.cos(angle)),
      top: round(50 + radius * Math.sin(angle)),
    }
  })
}
