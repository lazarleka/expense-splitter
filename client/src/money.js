// Iznosi se cuvaju i racunaju u centima (cijeli brojevi). Float se ne koristi za novac.

/**
 * "90,00", "90.00", "90" ili "1,5" -> cijeli broj centi. Vraca null za neispravan unos.
 */
export function parseAmountToCents(text) {
  const match = /^(\d+)(?:[.,](\d{1,2}))?$/.exec(text.trim())
  if (!match) return null

  const euros = Number(match[1])
  const cents = Number((match[2] ?? '').padEnd(2, '0'))
  return euros * 100 + cents
}

/**
 * 3333 -> "33,33"
 */
export function formatCents(cents) {
  const sign = cents < 0 ? '-' : ''
  const abs = Math.abs(cents)
  const euros = Math.floor(abs / 100)
  const rest = String(abs % 100).padStart(2, '0')
  return `${sign}${euros},${rest}`
}
