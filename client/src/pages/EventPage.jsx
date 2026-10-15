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
  const [friends, setFriends] = useState([])

  async function reload() {
    setEvent(await api(`/events/${id}`))
  }

  async function handleExpenseAdded() {
    setAddingExpense(false)
    await reload()
  }

  useEffect(() => {
    let ignore = false
    Promise.all([api(`/events/${id}`), api('/friends')])
      .then(([data, mine]) => {
        if (ignore) return
        setEvent(data)
        setFriends(mine.friends)
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
          400: 'Izaberite prijatelja.',
          403: 'Mozete pozvati samo svoje prijatelje.',
          404: 'Korisnik ne postoji.',
          409: 'Korisnik je vec clan, vec ima pozivnicu ili je dogadjaj arhiviran.',
        },
      })
      setInvitee('')
      setNotice(`Pozivnica je poslata korisniku ${invitation.invitee}.`)
      await reload()
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
  // Pozvati se mogu samo prijatelji koji nisu clanovi i nemaju pozivnicu na cekanju
  const invitable = friends.filter(
    (f) => !event.members.some((m) => m.userId === f.id) && !event.pendingInviteeIds.includes(f.id),
  )
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
          <h2>Pozovi prijatelja</h2>
          {invitable.length === 0 ? (
            <p>
              Nema prijatelja za poziv. <Link to="/friends">Dodajte prijatelje</Link>
            </p>
          ) : (
            <form className="row" onSubmit={handleInvite}>
              <select aria-label="Prijatelj za poziv" value={invitee} onChange={(e) => setInvitee(e.target.value)}>
                <option value="">Izaberite prijatelja</option>
                {invitable.map((f) => (
                  <option key={f.id} value={f.username}>{f.username}</option>
                ))}
              </select>
              <button type="submit" disabled={!invitee}>Pozovi</button>
            </form>
          )}
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
