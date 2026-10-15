import { formatCents } from '../money.js'
import { seatPositions } from '../tableLayout.js'

function balanceClass(cents) {
  if (cents > 0) return 'positive'
  if (cents < 0) return 'negative'
  return ''
}

/**
 * Clanovi sjede za okruglim stolom po redu ulaska. U sredini su ukupan trosak i dugme "+".
 */
function RoundTable({ members, totalCents, currentUserId, onAdd }) {
  const positions = seatPositions(members.length)

  return (
    <div className="round-table" role="group" aria-label="Sto sa clanovima dogadjaja">
      <div className="table-top">
        <span className="table-label">Ukupno</span>
        <strong className="table-total">{formatCents(totalCents)}</strong>
        {onAdd && (
          <button type="button" className="table-add" aria-label="Dodaj trosak" onClick={onAdd}>+</button>
        )}
      </div>

      {members.map((m, i) => (
        <div
          key={m.userId}
          className={`seat${m.userId === currentUserId ? ' me' : ''}`}
          style={{ left: `${positions[i].left}%`, top: `${positions[i].top}%` }}
        >
          <span className="seat-name">{m.username}</span>
          <span className={`seat-balance ${balanceClass(m.balanceCents)}`}>
            {m.balanceCents > 0 ? '+' : ''}{formatCents(m.balanceCents)}
          </span>
          {m.archiveConsent && <span className="seat-consent" title="Saglasan za arhiviranje">&#10003;</span>}
        </div>
      ))}
    </div>
  )
}

export default RoundTable
