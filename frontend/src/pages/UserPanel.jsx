import { useEffect, useState } from 'react'
import { api, egp } from '../api.js'
import { DonationTable, Receipt, Stat } from '../components.jsx'

export default function UserPanel({ user, onUserChange }) {
  const [dash, setDash] = useState(null)
  const [receipt, setReceipt] = useState('')
  const [error, setError] = useState('')

  useEffect(() => {
    api.myDonations().then(setDash).catch((e) => setError(e.message))
  }, [user])

  return (
    <section>
      <h1>My panel</h1>
      {error && <div className="error">{error}</div>}
      {dash && (
        <>
          <div className="stats">
            <Stat label="Total given" value={egp(dash.totalGiven)} />
            <Stat label="Donations" value={dash.donationCount} />
            <Stat label="Campaigns supported" value={dash.campaignsSupported} />
          </div>
          <h2>My donations</h2>
          <DonationTable
            donations={dash.donations}
            actions={(d) => (
              <button onClick={() => api.receipt(d.id).then(setReceipt)}>Receipt</button>
            )}
          />
        </>
      )}
      <ProfileForm user={user} onSaved={onUserChange} />
      <Receipt text={receipt} onClose={() => setReceipt('')} />
    </section>
  )
}

function ProfileForm({ user, onSaved }) {
  const [form, setForm] = useState({ name: user.name, email: user.email, currentPassword: '', newPassword: '' })
  const [msg, setMsg] = useState('')
  const [error, setError] = useState('')
  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value })

  const submit = async (e) => {
    e.preventDefault()
    setMsg('')
    setError('')
    try {
      const u = await api.editProfile(form)
      setForm({ ...form, currentPassword: '', newPassword: '' })
      setMsg('Profile saved.')
      onSaved(u)
    } catch (err) {
      setError(err.message)
    }
  }

  return (
    <>
      <h2>Edit profile</h2>
      <form className="panel-form" onSubmit={submit}>
        <div className="two">
          <label>Name<input value={form.name} onChange={set('name')} required /></label>
          <label>Email<input type="email" value={form.email} onChange={set('email')} required /></label>
        </div>
        <div className="two">
          <label>Current password<input type="password" value={form.currentPassword} onChange={set('currentPassword')} placeholder="Only to change password" /></label>
          <label>New password<input type="password" value={form.newPassword} onChange={set('newPassword')} minLength={6} /></label>
        </div>
        {error && <div className="error">{error}</div>}
        {msg && <div className="ok">{msg}</div>}
        <button className="primary" type="submit">Save profile</button>
      </form>
    </>
  )
}
