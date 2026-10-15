import { describe, expect, it } from 'vitest'
import { AVATARS, fallbackColor, initials } from './avatars.js'

describe('avatari', () => {
  it('skup ima 12 avatara, svaki sa emoji-jem i nazivom', () => {
    const all = Object.values(AVATARS)
    expect(all).toHaveLength(12)
    for (const avatar of all) {
      expect(avatar.emoji).toBeTruthy()
      expect(avatar.label).toBeTruthy()
    }
  })

  it('inicijali uzimaju prva slova dijelova imena', () => {
    expect(initials('ana_test')).toBe('AT')
    expect(initials('marko')).toBe('MA')
    expect(initials('iva.petrovic')).toBe('IP')
    expect(initials('x')).toBe('X')
  })

  it('boja inicijala je ista za istog korisnika', () => {
    expect(fallbackColor(3)).toBe(fallbackColor(3))
    expect(fallbackColor(3)).toBe(fallbackColor(11))
    expect(fallbackColor(1)).not.toBe(fallbackColor(2))
  })
})
