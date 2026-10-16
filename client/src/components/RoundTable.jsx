import { formatCents } from '../money.js'
import { PlusIcon } from './Icons.jsx'
import { seatAngle, seatPositions } from '../tableLayout.js'

// Poluprecnici u procentima sirine stola: stolice su uz ivicu stola, tanjiri na stolnjaku
const CHAIR_RADIUS = 41
const PLATE_RADIUS = 24

function balanceClass(cents) {
  if (cents > 0) return 'positive'
  if (cents < 0) return 'negative'
  return ''
}

// Stolica i tanjir se okrecu ka centru stola; natpis ostaje uspravan
function placeStyle(position, angle) {
  return { left: `${position.left}%`, top: `${position.top}%`, '--turn': `${angle + 90}deg` }
}

/**
 * Sto odozgo: clanovi sjede po redu ulaska, svako ima stolicu i tanjir.
 * Slobodna stolica sa "+" poziva prijatelja; "+" na sredini stola dodaje trosak.
 */
function RoundTable({ members, totalCents, currentUserId, onAdd, onInvite, inviteButtonRef, expenseButtonRef }) {
  const seatCount = members.length + (onInvite ? 1 : 0)
  const chairs = seatPositions(seatCount, CHAIR_RADIUS)
  const plates = seatPositions(seatCount, PLATE_RADIUS)
  const angles = chairs.map((_, i) => seatAngle(i, seatCount))

  return (
    <div className={`round-table${seatCount >= 8 ? ' crowded' : ''}`} role="group" aria-label="Sto sa clanovima dogadjaja">
      <span className="table-rug" aria-hidden="true" />

      {chairs.map((position, i) => {
        const member = members[i]
        const classes = ['chair', member ? '' : 'empty', member?.userId === currentUserId ? 'me' : '']
        return (
          <span key={member ? member.userId : 'free'} className={classes.filter(Boolean).join(' ')}
            style={placeStyle(position, angles[i])} aria-hidden="true" />
        )
      })}

      <div className="table-top">
        <span className="table-label">Ukupno</span>
        <strong className="table-total">{formatCents(totalCents)}</strong>
        {onAdd && (
          <button type="button" ref={expenseButtonRef} className="table-add" aria-label="Dodaj trosak" onClick={onAdd}>
            <PlusIcon size="55%" />
          </button>
        )}
      </div>

      {members.map((m, i) => (
        <span key={m.userId} className="plate" style={placeStyle(plates[i], angles[i])} aria-hidden="true" />
      ))}

      {members.map((m, i) => (
        <div
          key={m.userId}
          className={`seat${m.userId === currentUserId ? ' me' : ''}`}
          title={m.username}
          style={placeStyle(chairs[i], angles[i])}
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
          style={placeStyle(chairs[members.length], angles[members.length])}
          aria-label="Pozovi prijatelja za sto"
          onClick={onInvite}
        >
          <span className="seat-add-symbol" aria-hidden="true"><PlusIcon size="100%" /></span>
          <span>Dodaj clana</span>
        </button>
      )}
    </div>
  )
}

export default RoundTable
