// Raspored mjesta za okruglim stolom. Clanovi dolaze po redu ulaska: prvi (kreator) sjedi gore,
// ostali redom u smjeru kazaljke na satu, ravnomjerno po krugu.
// Ovo je raspored na ekranu (procenti), ne novac, pa float ovdje nije problem.

const round = (value) => Math.round(value * 100) / 100

/**
 * @param count broj clanova
 * @param radius poluprecnik kruga mjesta u procentima sirine kontejnera
 * @returns [{ left, top }] u procentima, centar mjesta
 */
export function seatPositions(count, radius = 42) {
  return Array.from({ length: count }, (_, i) => {
    const angle = -Math.PI / 2 + (2 * Math.PI * i) / count
    return {
      left: round(50 + radius * Math.cos(angle)),
      top: round(50 + radius * Math.sin(angle)),
    }
  })
}
