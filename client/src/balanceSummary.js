// Zbir stanja prijavljenog korisnika po aktivnim dogadjajima, u centima (cijeli brojevi, bez float-a).
// Dug i potrazivanje se sabiraju odvojeno: dug u jednom dogadjaju ne umanjuje potrazivanje u drugom.
// Arhivirani dogadjaji se ne racunaju.

/**
 * @param events odgovor GET /api/events: { active: [{ myBalanceCents }], archived: [...] }
 * @returns { oweCents, owedCents } koliko korisnik duguje i koliko njemu duguju
 */
export function summarizeBalances(events) {
  return events.active.reduce(
    (sum, e) => {
      if (e.myBalanceCents < 0) sum.oweCents -= e.myBalanceCents
      else sum.owedCents += e.myBalanceCents
      return sum
    },
    { oweCents: 0, owedCents: 0 },
  )
}
