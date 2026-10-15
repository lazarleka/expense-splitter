import { describe, expect, it } from 'vitest'
import { seatPositions } from './tableLayout.js'

describe('seatPositions', () => {
  it('prvi clan sjedi gore na sredini', () => {
    expect(seatPositions(1)).toEqual([{ left: 50, top: 8 }])
  })

  it('cetiri clana sjede gore, desno, dole, lijevo (smjer kazaljke)', () => {
    expect(seatPositions(4)).toEqual([
      { left: 50, top: 8 },
      { left: 92, top: 50 },
      { left: 50, top: 92 },
      { left: 8, top: 50 },
    ])
  })

  it('sva mjesta su jednako udaljena od centra stola', () => {
    for (const { left, top } of seatPositions(7, 40)) {
      expect(Math.hypot(left - 50, top - 50)).toBeCloseTo(40, 1)
    }
  })

  it('bez clanova nema mjesta', () => {
    expect(seatPositions(0)).toEqual([])
  })
})
