import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router'
import { api } from '../api.js'

function EventPage({ user }) {
  const { id } = useParams()
  const [event, setEvent] = useState(null)
  const [error, setError] = useState('')
  const [notFound, setNotFound] = useState(false)
  const [invitee, setInvitee] = useState('')
  const [notice, setNotice] = useState('')

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
        <h2>Clanovi</h2>
        <ol>
          {event.members.map((m) => (
            <li key={m.userId}>{m.username}</li>
          ))}
        </ol>
        {canInvite && (
          <form className="row" onSubmit={handleInvite}>
            <input
              aria-label="Korisnicko ime za poziv"
              placeholder="Korisnicko ime"
              value={invitee}
              onChange={(e) => setInvitee(e.target.value)}
            />
            <button type="submit">Pozovi</button>
          </form>
        )}
      </section>
    </>
  )
}

export default EventPage
