import { useEffect, useState } from 'react'
import { BrowserRouter, Navigate, Route, Routes } from 'react-router'
import './App.css'
import { api, setUnauthorizedHandler } from './api.js'
import EventPage from './pages/EventPage.jsx'
import EventsPage from './pages/EventsPage.jsx'
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
              <main className="app">
                <header className="topbar">
                  <h1>Expense Splitter</h1>
                  <span>
                    {user.username}{' '}
                    <button type="button" className="link" onClick={handleLogout}>Odjava</button>
                  </span>
                </header>
                <Routes>
                  <Route path="/" element={<EventsPage />} />
                  <Route path="/events/:id" element={<EventPage user={user} />} />
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
