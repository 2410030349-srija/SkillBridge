import { clearSession, getToken } from './auth'

const API_BASE =
  import.meta.env.VITE_API_BASE_URL ||
  (typeof window !== 'undefined' ? window.location.origin : 'http://localhost:8082')

async function request(path, options = {}) {
  const token = getToken()
  const headers = {
    ...(options.headers || {}),
  }

  if (options.body !== undefined) {
    headers['Content-Type'] = 'application/json'
  }
  if (token) {
    headers.Authorization = `Bearer ${token}`
  }

  let response
  try {
    response = await fetch(`${API_BASE}${path}`, {
      ...options,
      headers,
      body: options.body !== undefined ? JSON.stringify(options.body) : undefined,
    })
  } catch (error) {
    throw new Error(`Network error: ${error.message}`)
  }

  const text = await response.text()
  let data = {}
  try {
    data = text ? JSON.parse(text) : {}
  } catch {
    data = { message: text }
  }

  if (!response.ok) {
    if (response.status === 401) {
      const isAuthRequest = path.startsWith('/auth/login') || path.startsWith('/auth/register')
      if (isAuthRequest) {
        throw new Error(data.error || data.message || 'Invalid email or password.')
      }
      clearSession()
      if (typeof window !== 'undefined' && window.location.pathname !== '/login') {
        window.location.assign('/login')
      }
      throw new Error('Your session expired. Please log in again.')
    }
    if (response.status === 403) {
      throw new Error(data.error || data.message || 'Access denied (403). You do not have permission for this action.')
    }
    throw new Error(data.error || data.message || `HTTP ${response.status}: Request failed`)
  }

  return data
}

export const api = {
  health: () => request('/test'),
  signup: (payload) => request('/auth/register', { method: 'POST', body: payload }),
  login: (payload) => request('/auth/login', { method: 'POST', body: payload }),
  me: () => request('/profile/me'),
  domains: () => request('/profile/domains'),
  setupProfile: (payload) => request('/profile/setup', { method: 'PUT', body: payload }),
  verifyEmail: () => request('/profile/verify-email', { method: 'POST', body: {} }),
  updateRole: (role) => request('/profile/role', { method: 'PUT', body: { role } }),
  updateDomain: (domain) => request('/profile/domain', { method: 'PUT', body: { domain } }),

  feed: () => request('/home/feed'),
  searchContent: (keyword) => request(`/content/search?keyword=${encodeURIComponent(keyword)}`),
  contentDetails: (id) => request(`/content/${id}`),
  reactContent: (id, type) => request(`/content/${id}/reaction`, { method: 'POST', body: { type } }),
  bookmarkContent: (id) => request(`/content/${id}/bookmark`, { method: 'POST', body: {} }),
  removeBookmark: (id) => request(`/content/${id}/bookmark`, { method: 'DELETE' }),
  bookmarks: () => request('/content/bookmarks'),
  rateContent: (id, payload) => request(`/content/${id}/feedback`, { method: 'POST', body: payload }),
  myFeedback: () => request('/content/feedback'),

  createContent: (payload) => request('/content', { method: 'POST', body: payload }),
  updateContent: (id, payload) => request(`/content/${id}`, { method: 'PUT', body: payload }),
  deleteContent: (id) => request(`/content/${id}`, { method: 'DELETE' }),
  myContent: () => request('/content/mine'),

  pendingContent: () => request('/admin/content/pending'),
  approveContent: (id) => request(`/admin/content/${id}/approve`, { method: 'POST', body: {} }),
  rejectContent: (id) => request(`/admin/content/${id}/reject`, { method: 'DELETE' }),
  users: () => request('/admin/users'),
}
