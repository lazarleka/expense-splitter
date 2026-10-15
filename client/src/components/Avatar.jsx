import { AVATARS, fallbackColor, initials } from '../avatars.js'

/**
 * Avatar korisnika: izabrani emoji ili, dok ga nema, inicijali u boji korisnika.
 * user je { id, username, avatar } (clan dogadjaja ima userId umjesto id).
 */
function Avatar({ user, size = 32 }) {
  const id = user.id ?? user.userId
  const chosen = user.avatar ? AVATARS[user.avatar] : null
  const style = { width: size, height: size, fontSize: Math.round(size * (chosen ? 0.6 : 0.4)) }

  if (chosen) {
    return (
      <span className="avatar" style={style} role="img" aria-label={`Avatar: ${chosen.label}`}>
        {chosen.emoji}
      </span>
    )
  }
  return (
    <span
      className="avatar initials"
      style={{ ...style, background: fallbackColor(id) }}
      role="img"
      aria-label={`Avatar: ${user.username}`}
    >
      {initials(user.username)}
    </span>
  )
}

export default Avatar
