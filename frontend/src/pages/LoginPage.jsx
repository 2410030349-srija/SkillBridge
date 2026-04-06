import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import Card from '../components/Card'
import FormInput from '../components/FormInput'
import { api } from '../services/api'
import { clearSession, isProfileComplete, saveSession } from '../services/auth'

function LoginPage() {
  const navigate = useNavigate()
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [form, setForm] = useState({ email: '', password: '' })

  const handleSubmit = async (event) => {
    event.preventDefault()
    setLoading(true)
    setError('')

    try {
      const auth = await api.login(form)

      saveSession(auth, {
        id: auth.userId,
        email: auth.email,
        role: auth.role,
        profileCompleted: false,
      })

      const me = await api.me()
      saveSession(auth, me)

      if (isProfileComplete(me)) {
        navigate('/dashboard')
      } else {
        navigate('/profile-setup')
      }
    } catch (err) {
      clearSession()
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <main className="mx-auto flex min-h-[calc(100vh-72px)] w-full max-w-6xl items-center justify-center px-4 py-10 sm:px-6">
      <Card title="Welcome Back" subtitle="Log in to continue to your personalized dashboard." className="w-full max-w-md">
        <form onSubmit={handleSubmit} className="space-y-4">
          <FormInput
            id="login-email"
            label="Email"
            type="email"
            value={form.email}
            onChange={(event) => setForm((prev) => ({ ...prev, email: event.target.value }))}
            placeholder="you@example.com"
          />
          <FormInput
            id="login-password"
            label="Password"
            type="password"
            value={form.password}
            onChange={(event) => setForm((prev) => ({ ...prev, password: event.target.value }))}
            placeholder="Your password"
          />
          {error ? <p className="text-sm font-semibold text-rose-600">{error}</p> : null}
          <button type="submit" disabled={loading} className="w-full rounded-xl bg-slate-900 px-4 py-3 text-sm font-bold text-white hover:bg-slate-800 disabled:cursor-not-allowed disabled:opacity-60">
            {loading ? 'Logging in...' : 'Login'}
          </button>
        </form>
      </Card>
    </main>
  )
}

export default LoginPage
