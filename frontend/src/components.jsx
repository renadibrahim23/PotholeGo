import { egp } from './api.js'

export function ProgressBar({ campaign }) {
  return (
    <div className="progress-wrap">
      <div className="progress">
        <div
          className={campaign.goalReached ? 'progress-fill done' : 'progress-fill'}
          style={{ width: `${campaign.progress}%` }}
        />
      </div>
      <div className="progress-text">
        <strong>{egp(campaign.raisedAmount)}</strong> of {egp(campaign.goalAmount)}
        <span>{campaign.progress}%</span>
      </div>
    </div>
  )
}

export function Stat({ label, value }) {
  return (
    <div className="stat">
      <div className="stat-value">{value}</div>
      <div className="stat-label">{label}</div>
    </div>
  )
}

export function Receipt({ text, onClose }) {
  if (!text) return null
  return (
    <div className="modal-backdrop" onClick={onClose}>
      <div className="modal wide" onClick={(e) => e.stopPropagation()}>
        <pre className="receipt">{text}</pre>
        <div className="row-end">
          <button onClick={() => window.print()}>Print</button>
          <button className="primary" onClick={onClose}>Close</button>
        </div>
      </div>
    </div>
  )
}

export function DonationTable({ donations, actions }) {
  if (donations.length === 0) return <p className="muted">No donations yet.</p>
  return (
    <div className="table-wrap">
      <table>
        <thead>
          <tr>
            <th>#</th>
            <th>Date</th>
            <th>Donor</th>
            <th>Campaign</th>
            <th>Type</th>
            <th>Amount</th>
            <th>Details</th>
            {actions && <th />}
          </tr>
        </thead>
        <tbody>
          {donations.map((d) => (
            <tr key={d.id}>
              <td>{d.id}</td>
              <td>{d.date}</td>
              <td>{d.donorName}</td>
              <td>{d.campaignTitle}</td>
              <td><span className={`badge ${d.type.toLowerCase()}`}>{d.type}</span></td>
              <td>{d.amount > 0 ? egp(d.amount) : '—'}</td>
              <td>{d.details}</td>
              {actions && <td className="actions">{actions(d)}</td>}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
