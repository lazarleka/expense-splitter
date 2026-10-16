import { formatCents } from '../money.js'
import { seatPositions } from '../tableLayout.js'

function balanceClass(cents) {
  if (cents > 0) return 'positive'
  if (cents < 0) return 'negative'
  return ''
}

/**
 * Clanovi sjede za stolom po redu ulaska. Slobodno mjesto poziva prijatelja.
 */
function RoundTable({ members, totalCents, currentUserId, onAdd, onInvite, inviteButtonRef, expenseButtonRef }) {
  const seatCount = members.length + (onInvite ? 1 : 0)
  const positions = seatPositions(seatCount)

  return (
    <div className={`round-table${seatCount >= 8 ? ' crowded' : ''}`} role="group" aria-label="Sto sa clanovima dogadjaja">
      <div className="table-top">
        <span className="table-label">Ukupno</span>
        <strong className="table-total">{formatCents(totalCents)}</strong>
        {onAdd && (
          <button type="button" ref={expenseButtonRef} className="table-add" aria-label="Dodaj trosak" onClick={onAdd}>+</button>
        )}
      </div>

      {members.map((m, i) => (
        <div
          key={m.userId}
          className={`seat${m.userId === currentUserId ? ' me' : ''}`}
          title={m.username}
          style={{ left: `${positions[i].left}%`, top: `${positions[i].top}%` }}
        >
          <span className="seat-name">{m.username}</span>
          <span className={`seat-balance ${balanceClass(m.balanceCents)}`}>
            {m.balanceCents > 0 ? '+' : ''}{formatCents(m.balanceCents)}
          </span>
          {m.archiveConsent && <span className="seat-consent" title="Saglasan za arhiviranje">&#10003;</span>}
        </div>
      ))}
      {onInvite && (
        <button
          type="button"
          ref={inviteButtonRef}
          className="seat seat-add"
          style={{ left: `${positions[members.length].left}%`, top: `${positions[members.length].top}%` }}
          aria-label="Pozovi prijatelja za sto"
          onClick={onInvite}
        >
          <span className="seat-add-symbol" aria-hidden="true">+</span>
          <span>Dodaj clana</span>
        </button>
      )}
    </div>
  )
}

export default RoundTable
