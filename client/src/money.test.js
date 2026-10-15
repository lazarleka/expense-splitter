import { describe, expect, it } from 'vitest'
import { formatCents, parseAmountToCents } from './money.js'

describe('parseAmountToCents', () => {
  it('parsira zarez i tacku kao decimalni separator', () => {
    expect(parseAmountToCents('90,00')).toBe(9000)
    expect(parseAmountToCents('33.34')).toBe(3334)
  })

  it('prihvata cijele brojeve i jednu decimalu', () => {
    expect(parseAmountToCents('90')).toBe(9000)
    expect(parseAmountToCents('1,5')).toBe(150)
  })

  it('odbija neispravan unos', () => {
    expect(parseAmountToCents('')).toBeNull()
    expect(parseAmountToCents('abc')).toBeNull()
    expect(parseAmountToCents('1.234')).toBeNull()
    expect(parseAmountToCents('-5')).toBeNull()
  })

  it('sabira bez float gresaka (0,10 + 0,20 = 0,30)', () => {
    expect(parseAmountToCents('0,10') + parseAmountToCents('0,20')).toBe(parseAmountToCents('0,30'))
  })
})

describe('formatCents', () => {
  it('formatira cente u prikaz sa zarezom', () => {
    expect(formatCents(3333)).toBe('33,33')
    expect(formatCents(5)).toBe('0,05')
    expect(formatCents(4000)).toBe('40,00')
  })
})
