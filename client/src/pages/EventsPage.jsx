import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router'
import { api } from '../api.js'
import { PlusIcon } from '../components/Icons.jsx'
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
          <PlusIcon size={18} />
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
      <div className="event-card-head">
        <Link to={`/events/${event.id}`} className="event-card-title">{event.name}</Link>
      </div>
      <div className="event-card-total">{formatCents(event.totalCents)}</div>
      <div className="event-card-meta">{membersLabel(event.memberCount)}</div>
      <div className="event-card-balance muted">Arhiviran dogadjaj</div>
    </article>
  )
}

function EventsPage() {
  const navigate = useNavigate()
  const [invitations, setInvitations] = useState([])
  const [events, setEvents] = useState({ active: [], archived: [] })
  const [name, setName] = useState('')
  const [error, setError] = useState('')
  const [selectedTab, setSelectedTab] = useState('active')

  function handleTabKeyDown(event) {
    const tabs = ['active', 'archived']
    const index = tabs.indexOf(selectedTab)
    const next = event.key === 'ArrowRight' ? (index + 1) % tabs.length
      : event.key === 'ArrowLeft' ? (index + tabs.length - 1) % tabs.length
        : event.key === 'Home' ? 0 : event.key === 'End' ? tabs.length - 1 : -1
    if (next < 0) return
    event.preventDefault()
    setSelectedTab(tabs[next])
    event.currentTarget.parentElement.querySelectorAll('[role="tab"]')[next].focus()
  }

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

      <section className="card home-main" aria-label="Vasi dogadjaji">
        {error && <p className="error" role="alert">{error}</p>}

        <div className="event-tabs" role="tablist" aria-label="Vrsta dogadjaja">
          <button type="button" id="active-events-tab" role="tab" aria-controls="active-events-panel"
            aria-selected={selectedTab === 'active'} tabIndex={selectedTab === 'active' ? 0 : -1}
            onClick={() => setSelectedTab('active')} onKeyDown={handleTabKeyDown}>
            Aktivni dogadjaji <span className="count">{events.active.length}</span>
          </button>
          <button type="button" id="archived-events-tab" role="tab" aria-controls="archived-events-panel"
            aria-selected={selectedTab === 'archived'} tabIndex={selectedTab === 'archived' ? 0 : -1}
            onClick={() => setSelectedTab('archived')} onKeyDown={handleTabKeyDown}>
            Arhivirani <span className="count">{events.archived.length}</span>
          </button>
        </div>
        <div id="active-events-panel" role="tabpanel" aria-labelledby="active-events-tab" hidden={selectedTab !== 'active'}>
          {events.active.length === 0 ? (
            <p className="empty">Nemate aktivnih dogadjaja. Kreirajte prvi u polju lijevo.</p>
          ) : (
            <div className="event-grid">
              {events.active.map((e) => <EventCard key={e.id} event={e} />)}
            </div>
          )}
        </div>
        <div id="archived-events-panel" role="tabpanel" aria-labelledby="archived-events-tab" hidden={selectedTab !== 'archived'}>
          {events.archived.length === 0 ? (
            <p className="empty">Jos nemate arhiviranih dogadjaja.</p>
          ) : (
            <div className="event-grid compact">
              {events.archived.map((e) => <ArchivedCard key={e.id} event={e} />)}
            </div>
          )}
        </div>
      </section>
    </div>
  )
}

export default EventsPage
