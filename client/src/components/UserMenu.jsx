import { useEffect, useId, useRef, useState } from 'react'
import { NavLink } from 'react-router'
import { LogoutIcon, PersonIcon, UsersIcon } from './Icons.jsx'

/** Ikonica profila u zaglavlju; otvara meni sa imenom korisnika i odjavom. */
function UserMenu({ username, onLogout }) {
  const [open, setOpen] = useState(false)
  const rootRef = useRef(null)
  const buttonRef = useRef(null)
  const panelId = useId()

  // Zatvaranje klikom van menija ili tasterom Escape
  useEffect(() => {
    if (!open) return undefined
    function onPointerDown(e) {
      if (rootRef.current && !rootRef.current.contains(e.target)) setOpen(false)
    }
    function onKeyDown(e) {
      if (e.key === 'Escape') {
        setOpen(false)
        buttonRef.current?.focus()
      }
    }
    document.addEventListener('pointerdown', onPointerDown)
    document.addEventListener('keydown', onKeyDown)
    return () => {
      document.removeEventListener('pointerdown', onPointerDown)
      document.removeEventListener('keydown', onKeyDown)
    }
  }, [open])

  return (
    <div className="user-menu" ref={rootRef} onBlur={(e) => {
      if (!e.currentTarget.contains(e.relatedTarget)) setOpen(false)
    }}>
      <button
        type="button"
        className="user-menu-button"
        ref={buttonRef}
        aria-label="Korisnicki meni"
        aria-controls={panelId}
        aria-expanded={open}
        onClick={() => setOpen((o) => !o)}
      >
        <span className="user-menu-avatar"><PersonIcon size={18} /></span>
        <span className="user-menu-name">{username}</span>
        <svg className="user-menu-chevron" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" aria-hidden="true"><path d="m6 9 6 6 6-6" /></svg>
      </button>

      {open && (
        <div className="user-menu-panel" id={panelId}>
          <div className="user-menu-head">
            <span className="person-icon large"><PersonIcon size={24} /></span>
            <div>
              <div className="muted small-text">Prijavljeni ste kao</div>
              <strong>{username}</strong>
            </div>
          </div>
          <nav aria-label="Korisnicka navigacija">
            <NavLink to="/" end className="user-menu-item" onClick={() => setOpen(false)}>
              <PersonIcon size={18} /> Dogadjaji
            </NavLink>
            <NavLink to="/friends" className="user-menu-item" onClick={() => setOpen(false)}>
              <UsersIcon size={18} /> Prijatelji
            </NavLink>
          </nav>
          <div className="user-menu-divider" />
          <button type="button" className="user-menu-item danger" onClick={() => { setOpen(false); onLogout() }}>
            <LogoutIcon size={18} /> Odjavi se
          </button>
        </div>
      )}
    </div>
  )
}

export default UserMenu
