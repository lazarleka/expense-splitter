import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router'
import { api } from '../api.js'
import { formatCents } from '../money.js'

function fetchEvents() {
  return Promise.all([api('/invitations'), api('/events')])
}

// 1 clan, 2-4 clana, 5+ clanova (11-14 su uvijek "clanova")
function membersLabel(n) {
  const lastTwo = n % 100
  const last = n % 10
  if (last === 1 && lastTwo !== 11) return `${n} clan`
  if (last >= 2 && last <= 4 && (lastTwo < 12 || lastTwo > 14)) return `${n} clana`
  return `${n} clanova`
}

function balanceText(cents) {
  if (cents > 0) return { label: 'Potrazujete', className: 'positive', amount: `+${formatCents(cents)}` }
  if (cents < 0) return { label: 'Dugujete', className: 'negative', amount: formatCents(cents) }
  return { label: 'Izmireno', className: 'muted', amount: formatCents(0) }
}

function EventCard({ event }) {
  const balance = balanceText(event.myBalanceCents)
  return (
    <article className="card event-card">
      <div className="event-card-head">
        <Link to={`/events/${event.id}`} className="event-card-title">{event.name}</Link>
        <Link
          to={`/events/${event.id}?dodaj=1`}
          className="add-button"
          aria-label={`Dodaj trosak u ${event.name}`}
          title="Dodaj trosak"
        >
          +
        </Link>
      </div>
      <div className="event-card-total">{formatCents(event.totalCents)}</div>
      <div className="event-card-meta">
        {membersLabel(event.memberCount)} · kreirao {event.creator}
      </div>
      <div className="event-card-balance">
        {balance.label}: <strong className={balance.className}>{balance.amount}</strong>
      </div>
    </article>
  )
}

function ArchivedCard({ event }) {
  return (
    <article className="card event-card archived">
      <Link to={`/events/${event.id}`} className="event-card-title">{event.name}</Link>
      <div className="event-card-total">{formatCents(event.totalCents)}</div>
      <div className="event-card-meta">{membersLabel(event.memberCount)}</div>
    </article>
  )
}

function EventsPage() {
  const navigate = useNavigate()
  const [invitations, setInvitations] = useState([])
  const [events, setEvents] = useState({ active: [], archived: [] })
  const [name, setName] = useState('')
  const [error, setError] = useState('')

  function apply([pending, mine]) {
    setInvitations(pending)
    setEvents(mine)
  }

  async function reload() {
    apply(await fetchEvents())
  }

  useEffect(() => {
    let ignore = false
    fetchEvents()
      .then((data) => {
        if (!ignore) apply(data)
      })
      .catch((err) => {
        if (!ignore) setError(err.message)
      })
    return () => {
      ignore = true
    }
  }, [])

  async function handleCreate(event) {
    event.preventDefault()
    setError('')
    try {
      const created = await api('/events', {
        method: 'POST',
        body: { name },
        errors: { 400: 'Naziv dogadjaja je obavezan.' },
      })
      navigate(`/events/${created.id}`)
    } catch (err) {
      setError(err.message)
    }
  }

  async function answer(invitation, action) {
    setError('')
    try {
      await api(`/invitations/${invitation.id}/${action}`, {
        method: 'POST',
        errors: {
          404: 'Pozivnica vise ne vazi.',
          409: 'Dogadjaj je arhiviran.',
        },
      })
      await reload()
    } catch (err) {
      setError(err.message)
    }
  }

  return (
    <div className="home">
      <aside className="home-side">
        <section className="card">
          <h2>Novi dogadjaj</h2>
          <form className="stack" onSubmit={handleCreate}>
            <input
              aria-label="Naziv dogadjaja"
              placeholder="Npr. Izlet na Durmitor"
              value={name}
              onChange={(e) => setName(e.target.value)}
            />
            <button type="submit">Kreiraj dogadjaj</button>
          </form>
        </section>

        <section className="card">
          <h2>
            Pozivnice {invitations.length > 0 && <span className="count">{invitations.length}</span>}
          </h2>
          {invitations.length === 0 ? (
            <p className="muted">Nema pozivnica na cekanju.</p>
          ) : (
            invitations.map((i) => (
              <div key={i.id} className="invitation">
                <span>
                  <strong>{i.invitedBy}</strong> vas poziva na <strong>{i.eventName}</strong>
                </span>
                <span className="row">
                  <button type="button" onClick={() => answer(i, 'accept')}>Prihvati</button>
                  <button type="button" className="secondary" onClick={() => answer(i, 'decline')}>Odbij</button>
                </span>
              </div>
            ))
          )}
        </section>
      </aside>

      <div className="home-main">
        {error && <p className="error" role="alert">{error}</p>}

        <div className="section-head">
          <h2>Aktivni dogadjaji</h2>
          {events.active.length > 0 && <span className="count">{events.active.length}</span>}
        </div>
        {events.active.length === 0 ? (
          <p className="empty">Nemate aktivnih dogadjaja. Kreirajte prvi u polju lijevo.</p>
        ) : (
          <div className="event-grid">
            {events.active.map((e) => (
              <EventCard key={e.id} event={e} />
            ))}
          </div>
        )}

        {events.archived.length > 0 && (
          <>
            <div className="section-head">
              <h2>Arhivirani</h2>
            </div>
            <div className="event-grid compact">
              {events.archived.map((e) => (
                <ArchivedCard key={e.id} event={e} />
              ))}
            </div>
          </>
        )}
      </div>
    </div>
  )
}

export default EventsPage
