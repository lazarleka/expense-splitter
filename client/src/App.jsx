import { useEffect, useState } from 'react'
import { BrowserRouter, Link, Navigate, NavLink, Route, Routes } from 'react-router'
import './App.css'
import { api, setUnauthorizedHandler } from './api.js'
import EventPage from './pages/EventPage.jsx'
import EventsPage from './pages/EventsPage.jsx'
import FriendsPage from './pages/FriendsPage.jsx'
import LoginPage from './pages/LoginPage.jsx'

function App() {
  // undefined = jos ne znamo da li je korisnik prijavljen, null = nije
  const [user, setUser] = useState(undefined)

  useEffect(() => {
    setUnauthorizedHandler(() => setUser(null))
    api('/auth/me')
      .then(setUser)
      .catch(() => setUser(null))
  }, [])

  async function handleLogout() {
    try {
      await api('/auth/logout', { method: 'POST' })
    } finally {
      setUser(null)
    }
  }

  if (user === undefined) {
    return <main className="app"><p>Ucitavanje...</p></main>
  }

  return (
    <BrowserRouter>
      <Routes>
        <Route
          path="/login"
          element={user ? <Navigate to="/" replace /> : <LoginPage onLogin={setUser} />}
        />
        <Route
          path="*"
          element={
            user ? (
              <>
                <header className="topbar">
                  <div className="topbar-inner">
                    <Link to="/" className="brand">Expense Splitter</Link>
                    <nav className="tabs">
                      <NavLink to="/" end>Dogadjaji</NavLink>
                      <NavLink to="/friends">Prijatelji</NavLink>
                    </nav>
                    <span className="topbar-user">
                      {user.username}
                      <button type="button" className="link" onClick={handleLogout}>Odjava</button>
                    </span>
                  </div>
                </header>
                <main className="app">
                  <Routes>
                    <Route path="/" element={<EventsPage />} />
                    <Route path="/events/:id" element={<div className="narrow"><EventPage user={user} /></div>} />
                    <Route path="/friends" element={<div className="narrow"><FriendsPage /></div>} />
                    <Route path="*" element={<Navigate to="/" replace />} />
                  </Routes>
                </main>
              </>
            ) : (
              <Navigate to="/login" replace />
            )
          }
        />
      </Routes>
    </BrowserRouter>
  )
}

export default App
