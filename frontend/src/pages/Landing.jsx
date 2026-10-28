import { useEffect, useState } from 'react'
import { api, egp } from '../api.js'
import { ProgressBar } from '../components.jsx'

const STEPS = [
  { title: 'Residents report', text: 'Broken streetlights, potholes and unsafe sidewalks are reported by the people who live with them.' },
  { title: 'We open a campaign', text: 'Each repair gets a funding goal so everyone can see exactly what it costs.' },
  { title: 'You donate', text: 'Give online, by check, or donate materials like asphalt, paint and tools.' },
  { title: 'The street gets fixed', text: 'Track progress, download your receipt, and see your neighbourhood improve.' },
]

export default function Landing({ user, onNavigate, onLogin }) {
  const [campaigns, setCampaigns] = useState([])

  useEffect(() => { api.campaigns().then(setCampaigns).catch(() => setCampaigns([])) }, [])

  const raised = campaigns.reduce((s, c) => s + c.raisedAmount, 0)
  const donations = campaigns.reduce((s, c) => s + c.donationCount, 0)
  const featured = [...campaigns].sort((a, b) => b.progress - a.progress).slice(0, 3)

  return (
    <>
      <section className="hero">
        <div className="hero-inner">
          <h1>Safer streets, funded by the community.</h1>
          <p>
            Pothole Go is a civic non-profit that helps neighbourhoods fix broken roads,
            sidewalks and streetlights. Pick a repair and help pay for it.
          </p>
          <div className="hero-actions">
            <button className="primary big" onClick={() => onNavigate('campaigns')}>Browse campaigns</button>
            {!user && <button className="big" onClick={() => onLogin()}>Log in or sign up</button>}
          </div>
          <div className="hero-stats">
            <div><strong>{egp(raised)}</strong><span>raised</span></div>
            <div><strong>{campaigns.length}</strong><span>repair campaigns</span></div>
            <div><strong>{donations}</strong><span>donations</span></div>
          </div>
        </div>
      </section>

      <section className="page-section">
        <h2>How it works</h2>
        <div className="steps">
          {STEPS.map((s, i) => (
            <div key={s.title} className="step">
              <div className="step-num">{i + 1}</div>
              <h3>{s.title}</h3>
              <p className="muted">{s.text}</p>
            </div>
          ))}
        </div>
      </section>

      {featured.length > 0 && (
        <section className="page-section">
          <div className="section-head">
            <h2>Featured repairs</h2>
            <button onClick={() => onNavigate('campaigns')}>See all</button>
          </div>
          <div className="grid">
            {featured.map((c) => (
              <article key={c.id} className="card">
                <h3>{c.title}</h3>
                <ProgressBar campaign={c} />
              </article>
            ))}
          </div>
        </section>
      )}
    </>
  )
}
