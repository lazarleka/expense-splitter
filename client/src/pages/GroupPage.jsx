import { useEffect, useState } from 'react'
import { api } from '../api.js'
import { formatCents, parseAmountToCents } from '../money.js'

function GroupPage() {
  const [participants, setParticipants] = useState([])
  const [expenses, setExpenses] = useState([])
  const [transfers, setTransfers] = useState([])
  const [error, setError] = useState('')

  const [newName, setNewName] = useState('')
  const [title, setTitle] = useState('')
  const [amount, setAmount] = useState('')
  const [payerId, setPayerId] = useState('')

  function fetchAll() {
    return Promise.all([api('/participants'), api('/expenses'), api('/settlements')])
  }

  function applyAll([people, costs, debts]) {
    setParticipants(people)
    setExpenses(costs)
    setTransfers(debts)
  }

  async function reload() {
    applyAll(await fetchAll())
  }

  useEffect(() => {
    let ignore = false
    fetchAll()
      .then((data) => {
        if (!ignore) applyAll(data)
      })
      .catch(() => {
        if (!ignore) setError('Ne mogu da ucitam podatke. Da li je backend pokrenut?')
      })
    return () => {
      ignore = true
    }
  }, [])

  async function handleAddParticipant(event) {
    event.preventDefault()
    setError('')
    try {
      await api('/participants', {
        method: 'POST',
        body: { name: newName },
        errors: { 409: 'Ucesnik sa tim imenom vec postoji.' },
      })
      setNewName('')
      await reload()
    } catch (err) {
      setError(err.message)
    }
  }

  async function handleAddExpense(event) {
    event.preventDefault()
    setError('')
    const amountCents = parseAmountToCents(amount)
    if (amountCents === null || amountCents <= 0) {
      setError('Iznos mora biti pozitivan broj sa najvise dvije decimale.')
      return
    }
    try {
      await api('/expenses', {
        method: 'POST',
        body: { title, amountCents, payerId: Number(payerId) },
      })
      setTitle('')
      setAmount('')
      await reload()
    } catch (err) {
      setError(err.message)
    }
  }

  return (
    <>
      {error && <p className="error" role="alert">{error}</p>}

      <section className="card">
        <h2>Ucesnici</h2>
        <form className="row" onSubmit={handleAddParticipant}>
          <input
            aria-label="Ime ucesnika"
            placeholder="Ime"
            value={newName}
            onChange={(e) => setNewName(e.target.value)}
          />
          <button type="submit">Dodaj</button>
        </form>
        <ul>
          {participants.map((p) => (
            <li key={p.id}>{p.name}</li>
          ))}
        </ul>
      </section>

      <section className="card">
        <h2>Novi trosak</h2>
        <form className="stack" onSubmit={handleAddExpense}>
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
          <select
            aria-label="Ko je platio"
            value={payerId}
            onChange={(e) => setPayerId(e.target.value)}
            disabled={participants.length === 0}
          >
            <option value="">Ko je platio?</option>
            {participants.map((p) => (
              <option key={p.id} value={p.id}>{p.name}</option>
            ))}
          </select>
          <button type="submit" disabled={participants.length === 0}>Dodaj trosak</button>
        </form>
        <ul>
          {expenses.map((e) => (
            <li key={e.id}>
              {e.title}: {formatCents(e.amountCents)} ({e.payerName} platio)
            </li>
          ))}
        </ul>
      </section>

      <section className="card">
        <h2>Ko kome duguje</h2>
        {transfers.length === 0 ? (
          <p>Nema dugovanja.</p>
        ) : (
          <ul>
            {transfers.map((t, i) => (
              <li key={i}>
                {t.from} duguje {t.to}: {formatCents(t.amountCents)}
              </li>
            ))}
          </ul>
        )}
      </section>
    </>
  )
}

export default GroupPage
