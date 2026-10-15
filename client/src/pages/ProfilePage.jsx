import { useState } from 'react'
import { api } from '../api.js'
import { AVATARS } from '../avatars.js'
import Avatar from '../components/Avatar.jsx'

function ProfilePage({ user, onUserChange }) {
  const [error, setError] = useState('')

  async function choose(key) {
    setError('')
    try {
      const updated = await api('/profile/avatar', {
        method: 'PUT',
        body: { avatar: key },
        errors: { 400: 'Taj avatar ne postoji.' },
      })
      onUserChange(updated)
    } catch (err) {
      setError(err.message)
    }
  }

  return (
    <>
      {error && <p className="error" role="alert">{error}</p>}

      <section className="card">
        <h2>Moj avatar</h2>
        <div className="row profile-current">
          <Avatar user={user} size={56} />
          <strong>{user.username}</strong>
        </div>
        <div className="avatar-grid" role="group" aria-label="Izbor avatara">
          {Object.entries(AVATARS).map(([key, { emoji, label }]) => (
            <button
              key={key}
              type="button"
              className={`avatar-choice${user.avatar === key ? ' selected' : ''}`}
              aria-label={label}
              aria-pressed={user.avatar === key}
              onClick={() => choose(key)}
            >
              {emoji}
            </button>
          ))}
        </div>
      </section>
    </>
  )
}

export default ProfilePage
