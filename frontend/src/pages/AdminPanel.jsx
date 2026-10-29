import { useEffect, useState } from 'react'
import { api, egp } from '../api.js'
import { DonationTable, ProgressBar, Stat } from '../components.jsx'

export default function AdminPanel({ user }) {
  const [dash, setDash] = useState(null)
  const [users, setUsers] = useState([])
  const [error, setError] = useState('')
  const [editing, setEditing] = useState(null) // null, 'new', or a campaign
    const [notes, setNotes] = useState({ channel: 'Email', entries: [] })

  const load = () =>
    Promise.all([api.statistics(), api.users()])
      .then(([d, u]) => { setDash(d); setUsers(u) })
      .catch((e) => setError(e.message))
  useEffect(() => { load() }, [])

  const run = (p) => p.then(load).catch((e) => setError(e.message))

  if (!dash) return error ? <div className="error">{error}</div> : <p className="muted">Loading…</p>

  return (
    <section>
      <h1>Admin panel</h1>
      {error && <div className="error" onClick={() => setError('')}>{error}</div>}
      <div className="stats">
        <Stat label="Total raised" value={egp(dash.totalRaised)} />
        <Stat label="Campaigns" value={dash.campaignCount} />
        <Stat label="Fully funded" value={dash.campaignsFunded} />
        <Stat label="Donations" value={dash.donationCount} />
      </div>
      {Object.keys(dash.donationsByType).length > 0 && (
        <p className="muted">
          By type:{' '}
          {Object.entries(dash.donationsByType).map(([t, n]) => (
            <span key={t} className={`badge ${t.toLowerCase()}`}>{t}: {n}</span>
          ))}
        </p>
      )}

      <div className="section-head">
        <h2>Campaigns</h2>
        <button className="primary" onClick={() => setEditing('new')}>+ New campaign</button>
      </div>
      <div className="table-wrap">
        <table>
          <thead>
            <tr><th>#</th><th>Title</th><th style={{ width: '34%' }}>Progress</th><th>Donations</th><th /></tr>
          </thead>
          <tbody>
            {dash.campaigns.map((c) => (
              <tr key={c.id}>
                <td>{c.id}</td>
                <td>{c.title}</td>
                <td><ProgressBar campaign={c} /></td>
                <td>{c.donationCount}</td>
                <td className="actions">
                  <button onClick={() => setEditing(c)}>Edit</button>
                  <button
                    className="danger"
                    onClick={() => confirm(`Delete "${c.title}"?`) && run(api.deleteCampaign(c.id))}
                  >
                    Delete
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <h2>Recent donations</h2>
      <DonationTable
        donations={dash.recentDonations}
        actions={(d) => (
          <button
            className="danger"
            title="Remove a bounced check or a pledge that never arrived"
            onClick={() => confirm(`Remove donation #${d.id}?`) && run(api.removeDonation(d.id))}
          >
            Remove
          </button>
        )}
      />

      <h2>Users</h2>
      <div className="table-wrap">
        <table>
          <thead>
            <tr><th>#</th><th>Name</th><th>Email</th><th>Role</th></tr>
          </thead>
          <tbody>
            {users.map((u) => (
              <tr key={u.id}>
                <td>{u.id}</td>
                <td>{u.name}</td>
                <td>{u.email}</td>
                <td>
                  {u.id === user.id ? (
                    <span className="badge">{u.role} (you)</span>
                  ) : (
                    <select value={u.role} onChange={(e) => run(api.changeRole(u.id, e.target.value))}>
                      <option>Resident</option>
                      <option>Admin</option>
                    </select>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
        <div className="section-head">
            <h2>Notifications</h2>
            <div>
                <label style={{ display: 'inline-block', marginRight: '0.5rem' }}>
                    Channel{' '}
                    <select value={notes.channel} onChange={(e) => run(api.setChannel(e.target.value))}>
                        <option>Email</option>
                        <option>SMS</option>
                        <option>Push</option>
                    </select>
                </label>
                <button onClick={load}>Refresh</button>
            </div>
        </div>
        {notes.entries.length === 0 ? (
            <p className="muted">No notifications sent yet. They are sent to donors after each donation.</p>
        ) : (
            <div className="table-wrap">
                <table>
                    <thead>
                    <tr><th>Notification log (newest first)</th></tr>
                    </thead>
                    <tbody>
                    {notes.entries.slice(0, 30).map((n, i) => (
                        <tr key={i}><td>{n}</td></tr>
                    ))}
                    </tbody>
                </table>
            </div>
        )}
      {editing && (
        <CampaignForm
          campaign={editing === 'new' ? null : editing}
          onClose={() => setEditing(null)}
          onSave={(body) =>
            (editing === 'new' ? api.createCampaign(body) : api.updateCampaign(editing.id, body))
              .then(() => { setEditing(null); load() })
          }
        />
      )}
    </section>
  )
}

function CampaignForm({ campaign, onClose, onSave }) {
  const [title, setTitle] = useState(campaign?.title ?? '')
  const [goal, setGoal] = useState(campaign?.goalAmount ?? '')
  const [error, setError] = useState('')

  const submit = (e) => {
    e.preventDefault()
    onSave({ title, goalAmount: Number(goal) }).catch((err) => setError(err.message))
  }

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <form className="modal" onClick={(e) => e.stopPropagation()} onSubmit={submit}>
        <h2>{campaign ? 'Edit campaign' : 'New campaign'}</h2>
        <label>Title<input value={title} onChange={(e) => setTitle(e.target.value)} required /></label>
        <label>Goal (EGP)<input type="number" min="1" step="any" value={goal} onChange={(e) => setGoal(e.target.value)} required /></label>
        {error && <div className="error">{error}</div>}
        <div className="row-end">
          <button type="button" onClick={onClose}>Cancel</button>
          <button className="primary" type="submit">Save</button>
        </div>
      </form>
    </div>
  )
}
