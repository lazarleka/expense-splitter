import { describe, expect, it } from 'vitest'
import { summarizeBalances } from './balanceSummary.js'

describe('summarizeBalances', () => {
  // Scenarij "Pregled stanja u meniju"
  it('sabira dug i potrazivanje odvojeno, samo iz aktivnih dogadjaja', () => {
    const events = {
      active: [
        { name: 'Rucak i taksi', myBalanceCents: 5000 },
        { name: 'Izlet', myBalanceCents: -1100 },
        { name: 'Prijatelji', myBalanceCents: 0 },
      ],
      archived: [{ name: 'Ljetovanje', myBalanceCents: -2000 }],
    }

    expect(summarizeBalances(events)).toEqual({ oweCents: 1100, owedCents: 5000 })
  })

  // Scenarij "Pregled stanja u meniju": bez aktivnih dogadjaja
  it('bez aktivnih dogadjaja oba iznosa su nula', () => {
    expect(summarizeBalances({ active: [], archived: [{ myBalanceCents: 700 }] }))
      .toEqual({ oweCents: 0, owedCents: 0 })
  })

  it('dugovi iz vise dogadjaja se sabiraju', () => {
    const events = { active: [{ myBalanceCents: -333 }, { myBalanceCents: -1000 }], archived: [] }

    expect(summarizeBalances(events)).toEqual({ oweCents: 1333, owedCents: 0 })
  })
})
