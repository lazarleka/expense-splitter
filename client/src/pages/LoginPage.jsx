import { useState } from 'react'
import { api } from '../api.js'

const MIN_PASSWORD = 8

function LoginPage({ onLogin }) {
  const [mode, setMode] = useState('login')
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')

  const isRegister = mode === 'register'

  async function handleSubmit(event) {
    event.preventDefault()
    setError('')
    if (isRegister && password.length < MIN_PASSWORD) {
      setError(`Lozinka mora imati najmanje ${MIN_PASSWORD} znakova.`)
      return
    }
    try {
      const user = await api(isRegister ? '/auth/register' : '/auth/login', {
        method: 'POST',
        body: { username, password },
        errors: {
          400: 'Korisnicko ime i lozinka su obavezni.',
          401: 'Pogresno korisnicko ime ili lozinka.',
          409: 'Korisnicko ime je zauzeto.',
        },
      })
      onLogin(user)
    } catch (err) {
      setError(err.message)
    }
  }

  function switchMode() {
    setMode(isRegister ? 'login' : 'register')
    setError('')
  }

  return (
    <main className="app">
      <h1>Expense Splitter</h1>

      {error && <p className="error" role="alert">{error}</p>}

      <section className="card">
        <h2>{isRegister ? 'Registracija' : 'Prijava'}</h2>
        <form className="stack" onSubmit={handleSubmit}>
          <input
            aria-label="Korisnicko ime"
            placeholder="Korisnicko ime"
            autoComplete="username"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
          />
          <input
            aria-label="Lozinka"
            placeholder="Lozinka"
            type="password"
            autoComplete={isRegister ? 'new-password' : 'current-password'}
            value={password}
            onChange={(e) => setPassword(e.target.value)}
          />
          <button type="submit">{isRegister ? 'Registruj se' : 'Prijavi se'}</button>
        </form>
        <button type="button" className="link" onClick={switchMode}>
          {isRegister ? 'Vec imate nalog? Prijavite se' : 'Nemate nalog? Registrujte se'}
        </button>
      </section>
    </main>
  )
}

export default LoginPage
