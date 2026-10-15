import { useEffect, useState } from 'react'
import { api } from '../api.js'
import Avatar from '../components/Avatar.jsx'

const EMPTY = { friends: [], incoming: [], outgoing: [] }

function FriendsPage() {
  const [data, setData] = useState(EMPTY)
  const [username, setUsername] = useState('')
  const [error, setError] = useState('')
  const [notice, setNotice] = useState('')

  async function reload() {
    setData(await api('/friends'))
  }

  useEffect(() => {
    let ignore = false
    api('/friends')
      .then((friends) => {
        if (!ignore) setData(friends)
      })
      .catch((err) => {
        if (!ignore) setError(err.message)
      })
    return () => {
      ignore = true
    }
  }, [])

  async function handleRequest(event) {
    event.preventDefault()
    setError('')
    setNotice('')
    try {
      const sent = await api('/friends/requests', {
        method: 'POST',
        body: { username },
        errors: {
          400: 'Unesite korisnicko ime drugog korisnika.',
          404: 'Korisnik ne postoji.',
          409: 'Vec ste prijatelji ili zahtjev vec ceka odgovor.',
        },
      })
      setUsername('')
      setNotice(`Zahtjev je poslat korisniku ${sent.user.username}.`)
      await reload()
    } catch (err) {
      setError(err.message)
    }
  }

  async function answer(request, action) {
    setError('')
    setNotice('')
    try {
      await api(`/friends/requests/${request.id}/${action}`, {
        method: 'POST',
        errors: { 404: 'Zahtjev vise ne vazi.' },
      })
      await reload()
    } catch (err) {
      setError(err.message)
    }
  }

  return (
    <>
      {error && <p className="error" role="alert">{error}</p>}
      {notice && <p className="notice">{notice}</p>}

      <section className="card">
        <h2>Dodaj prijatelja</h2>
        <form className="row" onSubmit={handleRequest}>
          <input
            aria-label="Korisnicko ime prijatelja"
            placeholder="Korisnicko ime"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
          />
          <button type="submit">Posalji zahtjev</button>
        </form>
      </section>

      {data.incoming.length > 0 && (
        <section className="card">
          <h2>Zahtjevi za prijateljstvo</h2>
          <ul className="plain">
            {data.incoming.map((r) => (
              <li key={r.id} className="row between">
                <span className="row">
                  <Avatar user={r.user} />
                  <span><strong>{r.user.username}</strong> zeli da vam bude prijatelj</span>
                </span>
                <span className="row">
                  <button type="button" onClick={() => answer(r, 'accept')}>Prihvati</button>
                  <button type="button" className="secondary" onClick={() => answer(r, 'decline')}>Odbij</button>
                </span>
              </li>
            ))}
          </ul>
        </section>
      )}

      <section className="card">
        <h2>Prijatelji</h2>
        {data.friends.length === 0 ? (
          <p>Jos nemate prijatelja.</p>
        ) : (
          <ul className="plain">
            {data.friends.map((f) => (
              <li key={f.id} className="row">
                <Avatar user={f} />
                {f.username}
              </li>
            ))}
          </ul>
        )}
        {data.outgoing.length > 0 && (
          <p>
            <small>Ceka odgovor: {data.outgoing.map((r) => r.user.username).join(', ')}</small>
          </p>
        )}
      </section>
    </>
  )
}

export default FriendsPage
