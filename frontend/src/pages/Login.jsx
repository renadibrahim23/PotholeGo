import { useState } from 'react'
import { api } from '../api.js'

export default function Login({ onLoggedIn }) {
  const [mode, setMode] = useState('login')
  const [name, setName] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  const submit = async (e) => {
    e.preventDefault()
    setError('')
    setBusy(true)
    try {
      const u = mode === 'login'
        ? await api.login(email, password)
        : await api.register(name, email, password)
      onLoggedIn(u)
    } catch (err) {
      setError(err.message)
    } finally {
      setBusy(false)
    }
  }

  const switchTo = (m) => {
    setMode(m)
    setError('')
  }

  return (
    <section className="auth">
      <form className="auth-card" onSubmit={submit}>
        <h1>{mode === 'login' ? 'Welcome back' : 'Create an account'}</h1>
        <p className="muted">
          {mode === 'login' ? 'Log in to donate and see your receipts.' : 'Sign up to start supporting repairs.'}
        </p>
        {mode === 'register' && (
          <label>Full name<input value={name} onChange={(e) => setName(e.target.value)} required autoFocus /></label>
        )}
        <label>Email<input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required autoFocus={mode === 'login'} /></label>
        <label>Password<input type="password" value={password} onChange={(e) => setPassword(e.target.value)} required minLength={mode === 'register' ? 6 : undefined} /></label>
        {error && <div className="error">{error}</div>}
        <button className="primary full" type="submit" disabled={busy}>
          {mode === 'login' ? 'Log in' : 'Sign up'}
        </button>
        <p className="switch">
          {mode === 'login' ? (
            <>No account? <button type="button" className="link" onClick={() => switchTo('register')}>Sign up</button></>
          ) : (
            <>Already registered? <button type="button" className="link" onClick={() => switchTo('login')}>Log in</button></>
          )}
        </p>
      </form>
    </section>
  )
}
