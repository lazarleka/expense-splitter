// Prikaz avatara. Kljucevi moraju odgovarati enum-u Avatar na serveru (model/Avatar.java).

export const AVATARS = {
  FOX: { emoji: '🦊', label: 'lisica' },
  CAT: { emoji: '🐱', label: 'macka' },
  DOG: { emoji: '🐶', label: 'pas' },
  BEAR: { emoji: '🐻', label: 'medvjed' },
  PANDA: { emoji: '🐼', label: 'panda' },
  KOALA: { emoji: '🐨', label: 'koala' },
  LION: { emoji: '🦁', label: 'lav' },
  TIGER: { emoji: '🐯', label: 'tigar' },
  FROG: { emoji: '🐸', label: 'zaba' },
  OWL: { emoji: '🦉', label: 'sova' },
  PENGUIN: { emoji: '🐧', label: 'pingvin' },
  OCTOPUS: { emoji: '🐙', label: 'hobotnica' },
}

// Pozadine za inicijale; dovoljno tamne za bijela slova u obje teme
const COLORS = ['#4f46e5', '#0e7490', '#047857', '#b45309', '#be123c', '#7c3aed', '#1d4ed8', '#9d174d']

/** "ana_test" -> "AT", "marko" -> "MA" */
export function initials(username) {
  const parts = username.split(/[\s._-]+/).filter(Boolean)
  const letters = parts.length > 1 ? parts[0][0] + parts[1][0] : username.slice(0, 2)
  return letters.toUpperCase()
}

/** Boja zavisi samo od id-ja, pa je ista na svakom ekranu. */
export function fallbackColor(userId) {
  return COLORS[Math.abs(userId) % COLORS.length]
}
