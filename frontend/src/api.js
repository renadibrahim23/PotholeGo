async function request(method, url, body) {
  const res = await fetch(url, {
    method,
    headers: body ? { 'Content-Type': 'application/json' } : undefined,
    body: body ? JSON.stringify(body) : undefined,
  })
  if (!res.ok) {
    let message = `Request failed (${res.status})`
    try {
      const err = await res.json()
      if (err.message) message = err.message
    } catch {
      // body was not JSON
    }
    throw new Error(message)
  }
  if (res.status === 204) return null
  const type = res.headers.get('Content-Type') || ''
  return type.includes('json') ? res.json() : res.text()
}

export const api = {
  // AuthController
  me: () => request('GET', '/api/auth/me'),
  login: (email, password) => request('POST', '/api/auth/login', { email, password }),
  register: (name, email, password) => request('POST', '/api/auth/register', { name, email, password }),
  logout: () => request('POST', '/api/auth/logout'),

  // DonationController
  campaigns: () => request('GET', '/api/campaigns'),
  campaign: (id) => request('GET', `/api/campaigns/${id}`),
  donate: (d) => request('POST', '/api/donations', d),

  // UserPanelController
  myDonations: () => request('GET', '/api/user/donations'),
  receipt: (id) => request('GET', `/api/user/donations/${id}/receipt`),
  editProfile: (p) => request('PUT', '/api/user/profile', p),

  // AdminPanelController
  statistics: () => request('GET', '/api/admin/statistics'),
  createCampaign: (c) => request('POST', '/api/admin/campaigns', c),
  updateCampaign: (id, c) => request('PUT', `/api/admin/campaigns/${id}`, c),
  deleteCampaign: (id) => request('DELETE', `/api/admin/campaigns/${id}`),
  removeDonation: (id) => request('DELETE', `/api/admin/donations/${id}`),
  users: () => request('GET', '/api/admin/users'),
  changeRole: (id, role) => request('PUT', `/api/admin/users/${id}/role`, { role }),
}

export const egp = (n) =>
  n.toLocaleString('en-EG', { minimumFractionDigits: 0, maximumFractionDigits: 2 }) + ' EGP'
