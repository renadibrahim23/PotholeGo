import { useEffect, useState } from 'react'
import { api } from '../api.js'
import { ProgressBar, Receipt } from '../components.jsx'

const EMPTY = { type: 'Online', amount: '', checkNumber: '', bankName: '', itemName: '', quantity: '' ,
  paymentMethod: 'card', message: '', anonymous: false, }

export default function Campaigns({ user, onLogin }) {
  const [campaigns, setCampaigns] = useState([])
  const [selected, setSelected] = useState(null)
  const [error, setError] = useState('')

  const load = () => api.campaigns().then(setCampaigns).catch((e) => setError(e.message))
  useEffect(() => { load() }, [])

  const donate = (c) => (user ? setSelected(c) : onLogin())

  return (
    <section>
      <h1>Repair campaigns</h1>
      <p className="muted">Choose a repair and donate money, a check, or materials.</p>
      {error && <div className="error">{error}</div>}
      <div className="grid">
        {campaigns.map((c) => (
          <article key={c.id} className="card">
            <h3>{c.title}</h3>
            <ProgressBar campaign={c} />
            <div className="card-foot">
              <span className="muted">{c.donationCount} donation{c.donationCount === 1 ? '' : 's'}</span>
              <button className="primary" onClick={() => donate(c)}>
                {user ? 'Donate' : 'Log in to donate'}
              </button>
            </div>
          </article>
        ))}
      </div>
      {selected && (
        <DonateForm campaign={selected} onClose={() => setSelected(null)} onDone={load} />
      )}
    </section>
  )
}

function DonateForm({ campaign, onClose, onDone }) {
  const [form, setForm] = useState(EMPTY)
  const [error, setError] = useState('')
  const [receipt, setReceipt] = useState('')
  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value })

  const submit = async (e) => {
    e.preventDefault()
    setError('')
    try {
      const res = await api.donate({
        campaignId: campaign.id,
        type: form.type,
        amount: form.amount === '' ? null : Number(form.amount),
        checkNumber: form.checkNumber,
        bankName: form.bankName,
        itemName: form.itemName,
        quantity: form.quantity === '' ? null : Number(form.quantity),
        paymentMethod: form.type === 'Online' ? form.paymentMethod : null,
        message: form.message,
        anonymous: form.anonymous,
      })
      onDone()
      setReceipt(res.receipt)
    } catch (err) {
      setError(err.message)
    }
  }

  if (receipt) return <Receipt text={receipt} onClose={onClose} />

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <form className="modal" onClick={(e) => e.stopPropagation()} onSubmit={submit}>
        <h2>Donate to “{campaign.title}”</h2>
        <div className="segmented">
          {['Online', 'Check', 'InKind'].map((t) => (
              <button
                  type="button"
                  key={t}
                  className={form.type === t ? 'seg active' : 'seg'}
                  onClick={() => setForm({...form, type: t})}
              >
                {t === 'InKind' ? 'In-kind' : t}
              </button>
          ))}
        </div>
        {form.type !== 'InKind' && (
            <label>Amount (EGP)<input type="number" min="1" step="any" value={form.amount} onChange={set('amount')}
                                      required/></label>
        )}
        {form.type === 'Online' && (
            <label>Payment method
              <select value={form.paymentMethod} onChange={set('paymentMethod')}>
                <option value="card">Card</option>
                <option value="wallet">Mobile wallet</option>
                <option value="bank">Bank transfer</option>
              </select>
            </label>
        )}
        {form.type === 'Check' && (
            <div className="two">
              <label>Check number<input value={form.checkNumber} onChange={set('checkNumber')} required/></label>
              <label>Bank<input value={form.bankName} onChange={set('bankName')} required/></label>
            </div>
        )}
        {form.type === 'InKind' && (
            <div className="two">
              <label>Item<input value={form.itemName} onChange={set('itemName')} placeholder="e.g. Asphalt bags"
                                required/></label>
              <label>Quantity<input type="number" min="1" value={form.quantity} onChange={set('quantity')}
                                    required/></label>
            </div>
        )}
        <label>Message on receipt (optional)
          <input value={form.message} onChange={set('message')} placeholder="e.g. In memory of Grandpa"/>
        </label>
        <label>
          <input
              type="checkbox"
              checked={form.anonymous}
              onChange={(e) => setForm({ ...form, anonymous: e.target.checked })}
          />
          {' '}Donate anonymously (hides my name on the receipt)
        </label>
        {error && <div className="error">{error}</div>}
        <div className="row-end">
          <button type="button" onClick={onClose}>Cancel</button>
          <button className="primary" type="submit">Donate</button>
        </div>
      </form>
    </div>
  )
}
