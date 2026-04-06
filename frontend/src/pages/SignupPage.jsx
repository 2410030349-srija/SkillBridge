import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import Card from '../components/Card'
import FormInput from '../components/FormInput'
import { api } from '../services/api'
import { saveSession } from '../services/auth'

function SignupPage() {
  const navigate = useNavigate()
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [form, setForm] = useState({
    username: '',
    email: '',
    password: '',
    role: 'LEARNER',
  })

  const update = (key) => (event) => {
    setForm((prev) => ({ ...prev, [key]: event.target.value }))
  }

  const handleSubmit = async (event) => {
    event.preventDefault()
    setLoading(true)
    setError('')
    try {
      const auth = await api.signup({
        username: form.username,
        name: form.username,
        email: form.email,
        password: form.password,
        role: form.role,
        bio: '',
        teachSkills: [],
        learnSkills: [],
      })

      saveSession(auth, {
        id: auth.userId,
        email: auth.email,
        role: auth.role,
        profileCompleted: false,
      })

      const me = await api.me()
      saveSession(auth, me)
      navigate('/profile-setup')
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <main className="mx-auto flex min-h-[calc(100vh-72px)] w-full max-w-6xl items-center justify-center px-4 py-10 sm:px-6">
      <Card title="Create Your Account" subtitle="Start simple. You will complete profile setup in the next step." className="w-full max-w-md">
        <form onSubmit={handleSubmit} className="space-y-4">
          <FormInput id="signup-username" label="Username" value={form.username} onChange={update('username')} placeholder="your_username" />
          <FormInput id="signup-email" label="Email" type="email" value={form.email} onChange={update('email')} placeholder="you@example.com" />
          <FormInput id="signup-password" label="Password" type="password" value={form.password} onChange={update('password')} placeholder="Minimum 8 characters" />
          <FormInput
            id="signup-role"
            label="Role"
            options={[
              { value: 'LEARNER', label: 'Learner' },
              { value: 'CREATOR', label: 'Creator' },
            ]}
            value={form.role}
            onChange={update('role')}
          />
          {error ? <p className="text-sm font-semibold text-rose-600">{error}</p> : null}
          <button type="submit" disabled={loading} className="w-full rounded-xl bg-slate-900 px-4 py-3 text-sm font-bold text-white hover:bg-slate-800 disabled:cursor-not-allowed disabled:opacity-60">
            {loading ? 'Creating account...' : 'Sign Up'}
          </button>
        </form>
      </Card>
    </main>
  )
}

export default SignupPage
