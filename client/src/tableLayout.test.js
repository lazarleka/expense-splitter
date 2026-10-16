import { describe, expect, it } from 'vitest'
import { seatAngle, seatPositions } from './tableLayout.js'

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

describe('seatAngle', () => {
  it('prvo mjesto gleda gore, ostala idu u smjeru kazaljke', () => {
    expect([0, 1, 2, 3].map((i) => seatAngle(i, 4))).toEqual([-90, 0, 90, 180])
  })

  it('ugao odgovara poziciji iz seatPositions', () => {
    const positions = seatPositions(5, 40)
    positions.forEach(({ left, top }, i) => {
      const radians = (seatAngle(i, 5) * Math.PI) / 180
      expect(left).toBeCloseTo(50 + 40 * Math.cos(radians), 1)
      expect(top).toBeCloseTo(50 + 40 * Math.sin(radians), 1)
    })
  })
})
