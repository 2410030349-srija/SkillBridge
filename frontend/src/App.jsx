import { Navigate, Route, Routes, useNavigate } from 'react-router-dom'
import Navbar from './components/Navbar'
import DashboardPage from './pages/DashboardPage'
import LandingPage from './pages/LandingPage'
import LoginPage from './pages/LoginPage'
import ProfileSetupPage from './pages/ProfileSetupPage'
import SignupPage from './pages/SignupPage'
import { clearSession, getProfile, getToken, isProfileComplete } from './services/auth'

function RequireAuth({ children }) {
  const profile = getProfile()
  const token = getToken()
  if (!profile || !token) {
    return <Navigate to="/login" replace />
  }
  return children
}

function RequireProfileComplete({ children }) {
  const profile = getProfile()
  const token = getToken()
  if (!profile || !token) {
    return <Navigate to="/login" replace />
  }
  if (!isProfileComplete(profile)) {
    return <Navigate to="/profile-setup" replace />
  }
  return children
}

function RedirectAuthedHome() {
  const profile = getProfile()
  const token = getToken()
  if (!profile || !token) {
    return <LandingPage />
  }
  return isProfileComplete(profile)
    ? <Navigate to="/dashboard" replace />
    : <Navigate to="/profile-setup" replace />
}

function App() {
  const navigate = useNavigate()
  const profile = getProfile()
  const token = getToken()

  const handleLogout = () => {
    clearSession()
    navigate('/login')
  }

  return (
    <div className="min-h-screen">
      <Navbar isAuthed={Boolean(profile && token)} onLogout={handleLogout} />
      <Routes>
        <Route path="/" element={<RedirectAuthedHome />} />
        <Route path="/signup" element={<SignupPage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route
          path="/profile-setup"
          element={(
            <RequireAuth>
              <ProfileSetupPage />
            </RequireAuth>
          )}
        />
        <Route
          path="/dashboard"
          element={(
            <RequireProfileComplete>
              <DashboardPage />
            </RequireProfileComplete>
          )}
        />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </div>
  )
}

export default App
