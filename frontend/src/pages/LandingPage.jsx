import { Link } from 'react-router-dom'

function LandingPage() {
  return (
    <main className="mx-auto flex min-h-[calc(100vh-72px)] w-full max-w-6xl items-center px-4 py-12 sm:px-6">
      <section className="grid w-full gap-8 rounded-3xl border border-amber-100 bg-white p-8 shadow-sm lg:grid-cols-2 lg:p-12">
        <div>
          <p className="mb-3 inline-flex rounded-full bg-amber-100 px-3 py-1 text-xs font-bold uppercase tracking-wide text-amber-700">
            Personalized Learning
          </p>
          <h1 className="font-heading text-3xl font-extrabold tracking-tight text-slate-900 sm:text-5xl">
            SkillBridge - Personalized Learning Platform
          </h1>
          <p className="mt-4 text-slate-600">
            Start with a simple account, complete your profile, and unlock a role-based dashboard that adapts to your domain and interests.
          </p>
          <div className="mt-8 flex flex-wrap gap-3">
            <Link to="/login" className="rounded-xl bg-slate-900 px-5 py-3 text-sm font-bold text-white hover:bg-slate-800">
              Login
            </Link>
            <Link to="/signup" className="rounded-xl bg-amber-500 px-5 py-3 text-sm font-bold text-white hover:bg-amber-600">
              Sign Up
            </Link>
          </div>
        </div>
        <div className="rounded-2xl border border-amber-100 bg-gradient-to-br from-amber-50 to-orange-50 p-6">
          <h2 className="font-heading text-xl font-bold text-slate-900">Step-by-step journey</h2>
          <ol className="mt-4 space-y-3 text-sm text-slate-600">
            <li>1. Sign up or login</li>
            <li>2. Complete profile setup</li>
            <li>3. Enter role-based dashboard</li>
            <li>4. Get domain-personalized content feed</li>
          </ol>
        </div>
      </section>
    </main>
  )
}

export default LandingPage
