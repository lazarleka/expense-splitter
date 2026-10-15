import { useState } from 'react'
import { api } from '../api.js'
import { parseAmountToCents } from '../money.js'

function ExpenseForm({ eventId, members, defaultPayerId, onAdded, onCancel }) {
  const [title, setTitle] = useState('')
  const [amount, setAmount] = useState('')
  const [payerId, setPayerId] = useState(String(defaultPayerId ?? ''))
  const [error, setError] = useState('')

  async function handleSubmit(event) {
    event.preventDefault()
    setError('')
    const amountCents = parseAmountToCents(amount)
    if (amountCents === null || amountCents <= 0) {
      setError('Iznos mora biti pozitivan broj sa najvise dvije decimale.')
      return
    }
    try {
      await api(`/events/${eventId}/expenses`, {
        method: 'POST',
        body: { title, amountCents, payerId: Number(payerId) },
        errors: {
          400: 'Naziv i platilac su obavezni, a platilac mora biti clan.',
          409: 'Dogadjaj je arhiviran.',
        },
      })
      setTitle('')
      setAmount('')
      onAdded()
    } catch (err) {
      setError(err.message)
    }
  }

  return (
    <form className="stack" onSubmit={handleSubmit}>
      {error && <p className="error" role="alert">{error}</p>}
      <input
        aria-label="Naziv troska"
        placeholder="Naziv (npr. Rucak)"
        value={title}
        onChange={(e) => setTitle(e.target.value)}
      />
      <input
        aria-label="Iznos"
        placeholder="Iznos (npr. 90,00)"
        inputMode="decimal"
        value={amount}
        onChange={(e) => setAmount(e.target.value)}
      />
      <select aria-label="Ko je platio" value={payerId} onChange={(e) => setPayerId(e.target.value)}>
        <option value="">Ko je platio?</option>
        {members.map((m) => (
          <option key={m.userId} value={m.userId}>{m.username}</option>
        ))}
      </select>
      <div className="row">
        <button type="submit">Dodaj trosak</button>
        {onCancel && (
          <button type="button" className="secondary" onClick={onCancel}>Odustani</button>
        )}
      </div>
    </form>
  )
}

export default ExpenseForm
