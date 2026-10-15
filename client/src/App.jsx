import { useEffect, useState } from 'react'
import { BrowserRouter, Navigate, NavLink, Route, Routes } from 'react-router'
import './App.css'
import { api, setUnauthorizedHandler } from './api.js'
import Avatar from './components/Avatar.jsx'
import EventPage from './pages/EventPage.jsx'
import EventsPage from './pages/EventsPage.jsx'
import FriendsPage from './pages/FriendsPage.jsx'
import LoginPage from './pages/LoginPage.jsx'
import ProfilePage from './pages/ProfilePage.jsx'

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
              <main className="app">
                <header className="topbar">
                  <h1>Expense Splitter</h1>
                  <span className="row me-badge">
                    <NavLink to="/profile" className="row me-link" title="Moj avatar">
                      <Avatar user={user} size={28} />
                      {user.username}
                    </NavLink>
                    <button type="button" className="link" onClick={handleLogout}>Odjava</button>
                  </span>
                </header>
                <nav className="tabs">
                  <NavLink to="/" end>Dogadjaji</NavLink>
                  <NavLink to="/friends">Prijatelji</NavLink>
                </nav>
                <Routes>
                  <Route path="/" element={<EventsPage />} />
                  <Route path="/events/:id" element={<EventPage user={user} />} />
                  <Route path="/friends" element={<FriendsPage />} />
                  <Route path="/profile" element={<ProfilePage user={user} onUserChange={setUser} />} />
                  <Route path="*" element={<Navigate to="/" replace />} />
                </Routes>
              </main>
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
