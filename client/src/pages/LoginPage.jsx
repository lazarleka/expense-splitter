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
    <main className="landing">
      <header className="topbar">
        <div className="topbar-inner">
          <span className="brand">Expense Splitter</span>
        </div>
      </header>

      {/* Uvod i forma su u istom gridu, centrirani jedno naspram drugog; prednosti su red ispod */}
      <div className="landing-body">
        <section className="landing-intro">
          <span className="eyebrow">Troskovi u drustvu, bez svadje</span>
          <h1>Podijelite troskove bez racunanja na salveti.</h1>
          <p>
            Napravite dogadjaj, pozovite prijatelje i upisite ko je sta platio. Aplikacija racuna ko kome
            duguje, tacno do centa.
          </p>
        </section>

        <section className="card landing-form">
          <h2>{isRegister ? 'Napravite nalog' : 'Prijavite se'}</h2>
          {error && <p className="error" role="alert">{error}</p>}
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

        <ul className="features">
          <li><strong>Okrugli sto</strong>Svaki clan sjedi za stolom sa svojim stanjem.</li>
          <li><strong>Do centa</strong>Iznosi se racunaju u centima, bez gresaka zaokruzivanja.</li>
          <li><strong>Zajednicko arhiviranje</strong>Dogadjaj se zatvara tek kad se svi sloze.</li>
        </ul>
      </div>
    </main>
  )
}

export default LoginPage
