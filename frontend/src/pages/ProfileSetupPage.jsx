import { useEffect, useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import Card from '../components/Card'
import FormInput from '../components/FormInput'
import { api } from '../services/api'
import { getProfile, saveSession } from '../services/auth'

const domainOptions = [
  { value: 'DATA SCIENCE', label: 'AI / ML' },
  { value: 'SOFTWARE DEVELOPMENT', label: 'Web Development' },
  { value: 'DEVOPS', label: 'Cloud Computing' },
  { value: 'DATA SCIENCE', label: 'Data Science' },
  { value: 'BLOCKCHAIN', label: 'Cybersecurity' },
]

function ProfileSetupPage() {
  const navigate = useNavigate()
  const profile = useMemo(() => getProfile(), [])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [domains, setDomains] = useState([])
  const [form, setForm] = useState({
    fullName: profile?.name || '',
    role: profile?.role || 'LEARNER',
    domain: 'DATA SCIENCE',
    interests: '',
    bio: profile?.bio || '',
  })

  useEffect(() => {
    if (!profile) {
      navigate('/login')
      return
    }

    api.domains().then(setDomains).catch(() => {})
  }, [navigate, profile])

  const mappedDomainOptions = domains.length
    ? domains.map((domain) => ({ value: domain, label: domain }))
    : domainOptions

  const handleSubmit = async (event) => {
    event.preventDefault()
    setLoading(true)
    setError('')
    try {
      const updated = await api.setupProfile({
        fullName: form.fullName,
        role: form.role,
        domain: form.domain,
        interests: form.interests
          .split(',')
          .map((item) => item.trim())
          .filter(Boolean),
        bio: form.bio,
      })

      const auth = {
        token: localStorage.getItem('skillbridge.token') || '',
        email: updated.email,
        role: updated.role,
        userId: updated.id,
      }
      saveSession(auth, updated)
      navigate('/dashboard')
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <main className="mx-auto flex min-h-[calc(100vh-72px)] w-full max-w-6xl items-center justify-center px-4 py-10 sm:px-6">
      <Card title="Profile Setup" subtitle="Complete this once to unlock a personalized dashboard." className="w-full max-w-2xl">
        <form onSubmit={handleSubmit} className="grid gap-4 sm:grid-cols-2">
          <FormInput id="profile-full-name" label="Full Name" value={form.fullName} onChange={(event) => setForm((prev) => ({ ...prev, fullName: event.target.value }))} placeholder="Your full name" />
          <FormInput
            id="profile-role"
            label="Role"
            options={[
              { value: 'LEARNER', label: 'Learner' },
              { value: 'CREATOR', label: 'Creator' },
              { value: 'ADMIN', label: 'Admin' },
            ]}
            value={form.role}
            onChange={(event) => setForm((prev) => ({ ...prev, role: event.target.value }))}
          />

          <FormInput
            id="profile-domain"
            label="Domain"
            options={mappedDomainOptions}
            value={form.domain}
            onChange={(event) => setForm((prev) => ({ ...prev, domain: event.target.value }))}
          />
          <FormInput
            id="profile-interests"
            label="Interests (optional)"
            value={form.interests}
            onChange={(event) => setForm((prev) => ({ ...prev, interests: event.target.value }))}
            placeholder="AI, Web, Cloud"
          />
          <div className="sm:col-span-2">
            <FormInput
              id="profile-bio"
              label="Bio (optional)"
              multiline
              value={form.bio}
              onChange={(event) => setForm((prev) => ({ ...prev, bio: event.target.value }))}
              placeholder="Tell us about your learning goals"
            />
          </div>
          {error ? <p className="sm:col-span-2 text-sm font-semibold text-rose-600">{error}</p> : null}
          <div className="sm:col-span-2">
            <button type="submit" disabled={loading} className="w-full rounded-xl bg-amber-500 px-4 py-3 text-sm font-bold text-white hover:bg-amber-600 disabled:cursor-not-allowed disabled:opacity-60">
              {loading ? 'Saving profile...' : 'Continue'}
            </button>
          </div>
        </form>
      </Card>
    </main>
  )
}

export default ProfileSetupPage
