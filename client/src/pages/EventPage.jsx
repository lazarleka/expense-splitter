import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router'
import { api } from '../api.js'
import ExpenseForm from '../components/ExpenseForm.jsx'
import RoundTable from '../components/RoundTable.jsx'
import { formatCents } from '../money.js'

function EventPage({ user }) {
  const { id } = useParams()
  const [event, setEvent] = useState(null)
  const [error, setError] = useState('')
  const [notFound, setNotFound] = useState(false)
  const [invitee, setInvitee] = useState('')
  const [notice, setNotice] = useState('')
  const [addingExpense, setAddingExpense] = useState(false)

  async function reload() {
    setEvent(await api(`/events/${id}`))
  }

  async function handleExpenseAdded() {
    setAddingExpense(false)
    await reload()
  }

  useEffect(() => {
    let ignore = false
    api(`/events/${id}`)
      .then((data) => {
        if (!ignore) setEvent(data)
      })
      .catch((err) => {
        if (ignore) return
        if (err.status === 404) setNotFound(true)
        else setError(err.message)
      })
    return () => {
      ignore = true
    }
  }, [id])

  async function handleInvite(e) {
    e.preventDefault()
    setError('')
    setNotice('')
    try {
      const invitation = await api(`/events/${id}/invitations`, {
        method: 'POST',
        body: { username: invitee },
        errors: {
          400: 'Korisnicko ime je obavezno.',
          403: 'Samo kreator poziva clanove.',
          404: 'Korisnik ne postoji.',
          409: 'Korisnik je vec clan, vec ima pozivnicu ili je dogadjaj arhiviran.',
        },
      })
      setInvitee('')
      setNotice(`Pozivnica je poslata korisniku ${invitation.invitee}.`)
    } catch (err) {
      setError(err.message)
    }
  }

  async function handleConsent(consent) {
    setError('')
    setNotice('')
    try {
      await api(`/events/${id}/archive-consent`, {
        method: 'PUT',
        body: { consent },
        errors: { 409: 'Dogadjaj je vec arhiviran.' },
      })
      const updated = await api(`/events/${id}`)
      setEvent(updated)
      if (updated.archived) setNotice('Svi clanovi su saglasni. Dogadjaj je arhiviran.')
    } catch (err) {
      setError(err.message)
    }
  }

  if (notFound) {
    return (
      <section className="card">
        <p>Dogadjaj ne postoji.</p>
        <Link to="/">Nazad na dogadjaje</Link>
      </section>
    )
  }

  if (!event) {
    return error ? <p className="error" role="alert">{error}</p> : <p>Ucitavanje...</p>
  }

  const canInvite = event.creatorId === user.id && !event.archived
  const me = event.members.find((m) => m.userId === user.id)
  const consents = event.members.filter((m) => m.archiveConsent).length

  return (
    <>
      <Link to="/">&larr; Dogadjaji</Link>
      <h2>
        {event.name}
        {event.archived && <small> (arhiviran)</small>}
      </h2>

      {error && <p className="error" role="alert">{error}</p>}
      {notice && <p className="notice">{notice}</p>}

      <section className="card">
        <RoundTable
          members={event.members}
          totalCents={event.totalCents}
          currentUserId={user.id}
          onAdd={event.archived ? null : () => setAddingExpense(true)}
        />
        {addingExpense && (
          <>
            <h2>Novi trosak</h2>
            <ExpenseForm
              eventId={event.id}
              members={event.members}
              defaultPayerId={user.id}
              onAdded={handleExpenseAdded}
              onCancel={() => setAddingExpense(false)}
            />
          </>
        )}
      </section>

      {canInvite && (
        <section className="card">
          <h2>Pozovi clana</h2>
          <form className="row" onSubmit={handleInvite}>
            <input
              aria-label="Korisnicko ime za poziv"
              placeholder="Korisnicko ime"
              value={invitee}
              onChange={(e) => setInvitee(e.target.value)}
            />
            <button type="submit">Pozovi</button>
          </form>
        </section>
      )}

      {!event.archived && me && (
        <section className="card">
          <h2>Arhiviranje</h2>
          <p>
            Saglasno {consents} od {event.members.length} clanova. Dogadjaj se arhivira kad su svi saglasni,
            a novi trosak ili novi clan ponistava saglasnosti.
          </p>
          {me.archiveConsent ? (
            <button type="button" className="secondary" onClick={() => handleConsent(false)}>
              Povuci saglasnost
            </button>
          ) : (
            <button type="button" onClick={() => handleConsent(true)}>Saglasan sam sa arhiviranjem</button>
          )}
        </section>
      )}

      <section className="card">
        <h2>Troskovi</h2>
        {event.expenses.length === 0 ? (
          <p>Nema troskova.</p>
        ) : (
          <ul>
            {event.expenses.map((e) => (
              <li key={e.id}>
                {e.title}: {formatCents(e.amountCents)} ({e.payerName} platio)
              </li>
            ))}
          </ul>
        )}
      </section>

      <section className="card">
        <h2>Ko kome duguje</h2>
        {event.transfers.length === 0 ? (
          <p>Nema dugovanja.</p>
        ) : (
          <ul>
            {event.transfers.map((t, i) => (
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

export default EventPage
