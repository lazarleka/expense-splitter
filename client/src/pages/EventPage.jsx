import { useEffect, useRef, useState } from 'react'
import { Link, useParams, useSearchParams } from 'react-router'
import { api } from '../api.js'
import ExpenseForm from '../components/ExpenseForm.jsx'
import { ChevronLeftIcon } from '../components/Icons.jsx'
import RoundTable from '../components/RoundTable.jsx'
import { formatCents } from '../money.js'

function EventPage({ user }) {
  const { id } = useParams()
  const [searchParams] = useSearchParams()
  const [event, setEvent] = useState(null)
  const [error, setError] = useState('')
  const [notFound, setNotFound] = useState(false)
  const [notice, setNotice] = useState('')
  const [addingExpense, setAddingExpense] = useState(searchParams.get('dodaj') === '1')
  const [inviteOpen, setInviteOpen] = useState(false)
  const [inviteError, setInviteError] = useState('')
  const [inviting, setInviting] = useState(false)
  const [friends, setFriends] = useState([])
  const inviteButtonRef = useRef(null)
  const expenseButtonRef = useRef(null)
  const inviteDialogRef = useRef(null)
  const expenseDialogRef = useRef(null)

  async function reload() {
    setEvent(await api(`/events/${id}`))
  }

  async function handleExpenseAdded() {
    setAddingExpense(false)
    await reload()
    expenseButtonRef.current?.focus()
  }

  useEffect(() => {
    let ignore = false
    Promise.all([api(`/events/${id}`), api('/friends')])
      .then(([data, mine]) => {
        if (ignore) return
        setEvent(data)
        setFriends(mine.friends)
      })
      .catch((err) => {
        if (ignore) return
        if (err.status === 404) setNotFound(true)
        else setError(err.message)
      })
    return () => {
      ignore = true
    }
  }, [id])

  useEffect(() => {
    if (!inviteOpen && !addingExpense) return undefined
    function onKeyDown(e) {
      if (e.key === 'Escape') {
        if (inviteOpen) closeInvite()
        else closeExpense()
      }
      if (e.key === 'Tab') {
        const dialog = inviteOpen ? inviteDialogRef.current : expenseDialogRef.current
        const focusable = [...(dialog?.querySelectorAll('a[href], button:not(:disabled), input, select') ?? [])]
        if (focusable.length === 0) return
        const first = focusable[0]
        const last = focusable.at(-1)
        if (e.shiftKey && document.activeElement === first) {
          e.preventDefault()
          last.focus()
        } else if (!e.shiftKey && document.activeElement === last) {
          e.preventDefault()
          first.focus()
        }
      }
    }
    document.addEventListener('keydown', onKeyDown)
    return () => document.removeEventListener('keydown', onKeyDown)
  }, [inviteOpen, addingExpense])

  useEffect(() => {
    if (!inviteOpen && !addingExpense) return undefined
    const dialog = inviteOpen ? inviteDialogRef.current : expenseDialogRef.current
    const frame = requestAnimationFrame(() => dialog?.querySelector('button, input')?.focus())
    return () => cancelAnimationFrame(frame)
  }, [inviteOpen, addingExpense])

  function openInvite() {
    setInviteError('')
    setInviteOpen(true)
  }

  function closeInvite() {
    setInviteOpen(false)
    inviteButtonRef.current?.focus()
  }

  function closeExpense() {
    setAddingExpense(false)
    expenseButtonRef.current?.focus()
  }

  async function handleInvite(username) {
    setInviteError('')
    setNotice('')
    setInviting(true)
    try {
      const invitation = await api(`/events/${id}/invitations`, {
        method: 'POST',
        body: { username },
        errors: {
          400: 'Izaberite prijatelja.',
          403: 'Mozete pozvati samo svoje prijatelje.',
          404: 'Korisnik ne postoji.',
          409: 'Korisnik je vec clan, vec ima pozivnicu ili je dogadjaj arhiviran.',
        },
      })
      setNotice(`Pozivnica je poslata korisniku ${invitation.invitee}.`)
      await reload()
      closeInvite()
    } catch (err) {
      setInviteError(err.message)
    } finally {
      setInviting(false)
    }
  }

  async function handleConsent(consent) {
    setError('')
    setNotice('')
    try {
      await api(`/events/${id}/archive-consent`, {
        method: 'PUT',
        body: { consent },
        errors: { 409: 'Dogadjaj je vec arhiviran.' },
      })
      const updated = await api(`/events/${id}`)
      setEvent(updated)
      if (updated.archived) setNotice('Svi clanovi su saglasni. Dogadjaj je arhiviran.')
    } catch (err) {
      setError(err.message)
    }
  }

  if (notFound) {
    return (
      <section className="card">
        <p>Dogadjaj ne postoji.</p>
        <Link to="/">Nazad na dogadjaje</Link>
      </section>
    )
  }

  if (!event) {
    return error ? <p className="error" role="alert">{error}</p> : <p>Ucitavanje...</p>
  }

  const canInvite = event.creatorId === user.id && !event.archived
  const invitable = friends.filter(
    (f) => !event.members.some((m) => m.userId === f.id) && !event.pendingInviteeIds.includes(f.id),
  )
  const me = event.members.find((m) => m.userId === user.id)
  const consents = event.members.filter((m) => m.archiveConsent).length

  return (
    <div className="event-page">
      <div className="event-page-header">
        <Link to="/" className="event-back">
          <ChevronLeftIcon size={18} />
          Svi dogadjaji
        </Link>
        <div className="event-heading">
          <h1>{event.name}</h1>
        </div>
      </div>

      {error && <p className="error" role="alert">{error}</p>}
      {notice && <p className="notice" role="status">{notice}</p>}

      <div className="event-dashboard">
        <section className="card event-table-card" aria-label="Sto dogadjaja">
          <span className={`event-status${event.archived ? ' archived' : ''}`}>
            {event.archived ? 'Arhiviran' : 'Aktivan'}
          </span>
          <RoundTable
            members={event.members}
            totalCents={event.totalCents}
            currentUserId={user.id}
            onAdd={event.archived ? null : () => setAddingExpense(true)}
            onInvite={canInvite ? openInvite : null}
            inviteButtonRef={inviteButtonRef}
            expenseButtonRef={expenseButtonRef}
          />
        </section>

        <div className="event-details">
          <section className="card event-detail-card">
            <h2>Ko kome duguje</h2>
            {event.transfers.length === 0 ? (
              <p className="muted">Nema dugovanja.</p>
            ) : (
              <ul className="event-detail-list">
                {event.transfers.map((t, i) => (
                  <li key={i}>
                    <span><strong>{t.from}</strong> duguje {t.to}</span>
                    <strong>{formatCents(t.amountCents)}</strong>
                  </li>
                ))}
              </ul>
            )}
          </section>

          <section className="card event-detail-card expenses-card">
            <h2>Troskovi <span className="count">{event.expenses.length}</span></h2>
            {event.expenses.length === 0 ? (
              <p className="muted">Nema troskova.</p>
            ) : (
              <ul className="event-detail-list" tabIndex="0" aria-label="Lista troskova">
                {event.expenses.map((expense) => (
                  <li key={expense.id}>
                    <span><strong>{expense.title}</strong><small>{expense.payerName} platio</small></span>
                    <strong>{formatCents(expense.amountCents)}</strong>
                  </li>
                ))}
              </ul>
            )}
          </section>

          {/* Arhiviranje kao uska traka ispod troskova: ko je saglasan i dugme desno */}
          {!event.archived && me && (
            <section className="card archive-bar" aria-label="Arhiviranje">
              <div className="archive-bar-info">
                <div className="archive-bar-title">
                  <h2>Arhiviranje</h2>
                  <span className="count">{consents}/{event.members.length}</span>
                </div>
                <span className="muted small-text">
                  {consents === 0
                    ? 'Dogadjaj se arhivira kad svi clanovi daju saglasnost.'
                    : `Saglasni: ${event.members.filter((m) => m.archiveConsent).map((m) => m.username).join(', ')}`}
                </span>
              </div>
              <button type="button" className={me.archiveConsent ? 'secondary' : ''}
                onClick={() => handleConsent(!me.archiveConsent)}>
                {me.archiveConsent ? 'Povuci saglasnost' : 'Saglasan sam'}
              </button>
            </section>
          )}
        </div>
      </div>

      {inviteOpen && (
        <div className="event-modal-backdrop" onMouseDown={(e) => { if (e.target === e.currentTarget) closeInvite() }}>
          <section ref={inviteDialogRef} className="card event-modal" role="dialog" aria-modal="true"
            aria-labelledby="invite-title">
            <div className="event-modal-head">
              <div>
                <span className="muted small-text">Slobodno mjesto za stolom</span>
                <h2 id="invite-title">Pozovi prijatelja</h2>
              </div>
              <button type="button" className="secondary modal-close" aria-label="Zatvori" onClick={closeInvite}>
                &times;
              </button>
            </div>
            {inviteError && <p className="error" role="alert">{inviteError}</p>}
            {invitable.length === 0 ? (
              <p className="muted">Nema prijatelja za poziv. <Link to="/friends">Pronadjite prijatelje</Link>.</p>
            ) : (
              <div className="invite-list">
                {invitable.map((friend) => (
                  <button key={friend.id} type="button" className="invite-option"
                    disabled={inviting} onClick={() => handleInvite(friend.username)}>
                    <span>{friend.username}</span><span>Pozovi &rarr;</span>
                  </button>
                ))}
              </div>
            )}
          </section>
        </div>
      )}

      {addingExpense && !event.archived && (
        <div className="event-modal-backdrop" onMouseDown={(e) => { if (e.target === e.currentTarget) closeExpense() }}>
          <section ref={expenseDialogRef} className="card event-modal" role="dialog" aria-modal="true"
            aria-labelledby="expense-title">
            <div className="event-modal-head">
              <h2 id="expense-title">Novi trosak</h2>
              <button type="button" className="secondary modal-close" aria-label="Zatvori"
                onClick={closeExpense}>&times;</button>
            </div>
            <ExpenseForm eventId={event.id} members={event.members} defaultPayerId={user.id}
              onAdded={handleExpenseAdded} onCancel={closeExpense} />
          </section>
        </div>
      )}
    </div>
  )
}

export default EventPage
