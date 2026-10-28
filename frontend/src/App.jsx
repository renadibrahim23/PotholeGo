import { useEffect, useState } from 'react'
import { api } from './api.js'
import Landing from './pages/Landing.jsx'
import Login from './pages/Login.jsx'
import Campaigns from './pages/Campaigns.jsx'
import UserPanel from './pages/UserPanel.jsx'
import AdminPanel from './pages/AdminPanel.jsx'

export default function App() {
  const [user, setUser] = useState(null)
  const [ready, setReady] = useState(false)
  const [page, setPage] = useState('landing')
  // where to go after logging in (e.g. back to the donate button)
  const [afterLogin, setAfterLogin] = useState('campaigns')

  useEffect(() => {
    api.me().then(setUser).catch(() => setUser(null)).finally(() => setReady(true))
  }, [])

  const isAdmin = user?.role === 'Admin'

  const goLogin = (next = 'campaigns') => {
    setAfterLogin(next)
    setPage('login')
  }

  const onLoggedIn = (u) => {
    setUser(u)
    setPage(u.role === 'Admin' && afterLogin === 'campaigns' ? 'admin' : afterLogin)
  }

  const logout = async () => {
    await api.logout()
    setUser(null)
    setPage('landing')
  }

  // pages that need a login fall back to the login page
  let current = page
  if ((page === 'user' || page === 'admin') && !user) current = 'login'
  if (page === 'admin' && user && !isAdmin) current = 'user'

  const tabs = [
    { key: 'landing', label: 'Home' },
    { key: 'campaigns', label: 'Campaigns' },
    ...(user ? [{ key: 'user', label: 'My Panel' }] : []),
    ...(isAdmin ? [{ key: 'admin', label: 'Admin Panel' }] : []),
  ]

  if (!ready) return null

  return (
    <div className="app">
      <header className="topbar">
        <button className="brand" onClick={() => setPage('landing')}>
          <span className="logo">●</span> Pothole Go
        </button>
        <nav>
          {tabs.map((t) => (
            <button
              key={t.key}
              className={current === t.key ? 'tab active' : 'tab'}
              onClick={() => setPage(t.key)}
            >
              {t.label}
            </button>
          ))}
          {user ? (
            <>
              <span className="who">{user.name}</span>
              <button onClick={logout}>Log out</button>
            </>
          ) : (
            <button className="primary" onClick={() => goLogin()}>Log in</button>
          )}
        </nav>
      </header>
      <main className={current === 'landing' ? 'wide' : ''}>
        {current === 'landing' && <Landing user={user} onNavigate={setPage} onLogin={goLogin} />}
        {current === 'login' && <Login onLoggedIn={onLoggedIn} />}
        {current === 'campaigns' && <Campaigns user={user} onLogin={() => goLogin('campaigns')} />}
        {current === 'user' && <UserPanel user={user} onUserChange={setUser} />}
        {current === 'admin' && <AdminPanel user={user} />}
      </main>
    </div>
  )
}
