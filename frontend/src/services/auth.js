const TOKEN_KEY = 'skillbridge.token'
const PROFILE_KEY = 'skillbridge.profile'

export function saveSession(auth, profile) {
  localStorage.setItem(TOKEN_KEY, auth.token)
  localStorage.setItem(PROFILE_KEY, JSON.stringify(profile))
}

export function getToken() {
  return localStorage.getItem(TOKEN_KEY) || ''
}

export function getProfile() {
  const raw = localStorage.getItem(PROFILE_KEY)
  if (!raw) return null
  try {
    return JSON.parse(raw)
  } catch {
    return null
  }
}

export function clearSession() {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(PROFILE_KEY)
}

export function isProfileComplete(profile) {
  return Boolean(profile?.profileCompleted)
}
