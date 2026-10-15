import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router'
import { api } from '../api.js'

function fetchEvents() {
  return Promise.all([api('/invitations'), api('/events')])
}

function EventList({ title, events, empty }) {
  return (
    <section className="card">
      <h2>{title}</h2>
      {events.length === 0 ? (
        <p>{empty}</p>
      ) : (
        <ul>
          {events.map((e) => (
            <li key={e.id}>
              <Link to={`/events/${e.id}`}>{e.name}</Link> <small>(kreirao {e.creator})</small>
            </li>
          ))}
        </ul>
      )}
    </section>
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
    <>
      {error && <p className="error" role="alert">{error}</p>}

      {invitations.length > 0 && (
        <section className="card">
          <h2>Pozivnice</h2>
          <ul className="plain">
            {invitations.map((i) => (
              <li key={i.id} className="row between">
                <span>{i.invitedBy} vas poziva na <strong>{i.eventName}</strong></span>
                <span className="row">
                  <button type="button" onClick={() => answer(i, 'accept')}>Prihvati</button>
                  <button type="button" className="secondary" onClick={() => answer(i, 'decline')}>Odbij</button>
                </span>
              </li>
            ))}
          </ul>
        </section>
      )}

      <section className="card">
        <h2>Novi dogadjaj</h2>
        <form className="row" onSubmit={handleCreate}>
          <input
            aria-label="Naziv dogadjaja"
            placeholder="Naziv (npr. Izlet na Durmitor)"
            value={name}
            onChange={(e) => setName(e.target.value)}
          />
          <button type="submit">Kreiraj</button>
        </form>
      </section>

      <EventList title="Aktivni dogadjaji" events={events.active} empty="Nema aktivnih dogadjaja." />
      <EventList title="Arhivirani dogadjaji" events={events.archived} empty="Nema arhiviranih dogadjaja." />
    </>
  )
}

export default EventsPage
