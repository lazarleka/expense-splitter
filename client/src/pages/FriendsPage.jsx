import { useEffect, useState } from 'react'
import { api } from '../api.js'
import { PersonIcon, SearchIcon } from '../components/Icons.jsx'

const EMPTY = { friends: [], incoming: [], outgoing: [] }

const REQUEST_ERRORS = {
  400: 'Ne mozete dodati sebe.',
  404: 'Korisnik ne postoji.',
  409: 'Vec ste prijatelji ili zahtjev vec ceka odgovor.',
}

function Person({ name, children }) {
  return (
    <div className="person">
      <span className="person-icon"><PersonIcon size={18} /></span>
      <span className="person-name">{name}</span>
      <span className="person-actions">{children}</span>
    </div>
  )
}

function FriendsPage() {
  const [data, setData] = useState(EMPTY)
  const [query, setQuery] = useState('')
  const [results, setResults] = useState([])
  const [loadingUsers, setLoadingUsers] = useState(true)
  const [filter, setFilter] = useState('')
  const [error, setError] = useState('')
  const [notice, setNotice] = useState('')
  // Povecava se posle svake akcije, da se rezultati pretrage osvjeze sa novim odnosima
  const [version, setVersion] = useState(0)

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
  }, [version])

  // Ucitaj cijeli spisak; filtriranje po imenu se radi odmah na stranici.
  useEffect(() => {
    let ignore = false
    api('/friends/discover')
      .then((found) => {
        if (!ignore) setResults(found)
      })
      .catch((err) => {
        if (!ignore) setError(err.message)
      })
      .finally(() => {
        if (!ignore) setLoadingUsers(false)
      })
    return () => {
      ignore = true
    }
  }, [version])

  async function act(path, body, success, errors) {
    setError('')
    setNotice('')
    try {
      await api(path, { method: 'POST', body, errors })
      if (success) setNotice(success)
      setVersion((v) => v + 1)
    } catch (err) {
      setError(err.message)
    }
  }

  const sendRequest = (username) =>
    act('/friends/requests', { username }, `Zahtjev je poslat korisniku ${username}.`, REQUEST_ERRORS)
  const answer = (id, action) =>
    act(`/friends/requests/${id}/${action}`, undefined, null, { 404: 'Zahtjev vise ne vazi.' })
  const cancel = (id, username) =>
    act(`/friends/requests/${id}/cancel`, undefined, `Zahtjev za ${username} je povucen.`, {
      404: 'Zahtjev vise ne ceka odgovor.',
    })

  function resultAction(r) {
    switch (r.relation) {
      case 'FRIEND':
        return <span className="tag">Prijatelj</span>
      case 'OUTGOING':
        return (
          <>
            <span className="tag muted-tag">Zahtjev poslat</span>
            <button type="button" className="secondary small" onClick={() => cancel(r.requestId, r.username)}>
              Povuci
            </button>
          </>
        )
      case 'INCOMING':
        return (
          <button type="button" className="small" onClick={() => answer(r.requestId, 'accept')}>
            Prihvati
          </button>
        )
      default:
        return (
          <button type="button" className="small" onClick={() => sendRequest(r.username)}>
            Dodaj
          </button>
        )
    }
  }

  const visibleFriends = data.friends.filter((f) => f.username.toLowerCase().includes(filter.trim().toLowerCase()))
  const visibleUsers = results.filter((r) => r.username.toLowerCase().includes(query.trim().toLowerCase()))

  return (
    <div className="friends">
      {(error || notice) && (
        <div className="friends-feedback">
          {error && <p className="error" role="alert">{error}</p>}
          {notice && <p className="notice">{notice}</p>}
        </div>
      )}
      <aside className="friends-side">
        <section className="card discover-card">
          <h2>Pronadjite ljude</h2>
          <label className="search-field">
            <SearchIcon size={18} />
            <input
              aria-label="Pretraga korisnika"
              placeholder="Pretrazite korisnike"
              value={query}
              onChange={(e) => setQuery(e.target.value)}
            />
          </label>
          <div className="search-results" role="region" aria-label="Lista korisnika" tabIndex="0">
            {loadingUsers ? (
              <p className="muted">Ucitavanje korisnika...</p>
            ) : visibleUsers.length === 0 ? (
              <p className="muted">{query.trim() ? `Nema korisnika za "${query.trim()}".` : 'Nema drugih korisnika.'}</p>
            ) : (
              visibleUsers.map((r) => (
                <Person key={r.id} name={r.username}>{resultAction(r)}</Person>
              ))
            )}
          </div>
        </section>

        {data.outgoing.length > 0 && (
          <section className="card">
            <h2>Poslati zahtjevi <span className="count">{data.outgoing.length}</span></h2>
            {data.outgoing.map((r) => (
              <Person key={r.id} name={r.user.username}>
                <button type="button" className="secondary small" onClick={() => cancel(r.id, r.user.username)}>
                  Povuci
                </button>
              </Person>
            ))}
          </section>
        )}
      </aside>

      <div className="friends-main">
        <section className="card friends-list">
          <div className="section-head between">
            <div className="row center">
              <h2>Prijatelji</h2>
              <span className="count">{data.friends.length}</span>
            </div>
            {data.friends.length > 3 && (
              <input
                className="filter"
                aria-label="Filtriraj prijatelje"
                placeholder="Filtriraj..."
                value={filter}
                onChange={(e) => setFilter(e.target.value)}
              />
            )}
          </div>
          {data.friends.length === 0 ? (
            <p className="empty">Jos nemate prijatelja. Pronadjite ih u listi lijevo i posaljite zahtjev.</p>
          ) : visibleFriends.length === 0 ? (
            <p className="empty">Nijedan prijatelj ne odgovara filteru.</p>
          ) : (
            <div className="friend-grid">
              {visibleFriends.map((f) => (
                <div key={f.id} className="card friend-card">
                  <span className="person-icon large"><PersonIcon size={26} /></span>
                  <strong>{f.username}</strong>
                  <span className="muted small-text">Prijatelj</span>
                </div>
              ))}
            </div>
          )}
        </section>

        {data.incoming.length > 0 && (
          <section className="card highlight">
            <h2>Zahtjevi za prijateljstvo <span className="count">{data.incoming.length}</span></h2>
            {data.incoming.map((r) => (
              <Person key={r.id} name={r.user.username}>
                <button type="button" className="small" onClick={() => answer(r.id, 'accept')}>Prihvati</button>
                <button type="button" className="secondary small" onClick={() => answer(r.id, 'decline')}>
                  Odbij
                </button>
              </Person>
            ))}
          </section>
        )}
      </div>
    </div>
  )
}

export default FriendsPage
