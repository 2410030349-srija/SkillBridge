import { Link, useLocation } from 'react-router-dom'

function Navbar({ isAuthed, onLogout }) {
  const location = useLocation()

  return (
    <header className="sticky top-0 z-20 border-b border-amber-100 bg-white/90 backdrop-blur">
      <div className="mx-auto flex w-full max-w-6xl items-center justify-between px-4 py-3 sm:px-6">
        <Link to="/" className="flex items-center gap-2">
          <span className="inline-flex h-9 w-9 items-center justify-center rounded-xl bg-amber-500 text-sm font-extrabold text-white">SB</span>
          <span className="font-heading text-lg font-bold text-slate-900">SkillBridge</span>
        </Link>

        <nav className="flex items-center gap-2">
          {!isAuthed ? (
            <>
              <Link
                to="/login"
                className={`rounded-lg px-3 py-2 text-sm font-semibold transition ${location.pathname === '/login' ? 'bg-amber-100 text-amber-800' : 'text-slate-700 hover:bg-slate-100'}`}
              >
                Login
              </Link>
              <Link
                to="/signup"
                className={`rounded-lg px-3 py-2 text-sm font-semibold transition ${location.pathname === '/signup' ? 'bg-slate-900 text-white' : 'bg-amber-500 text-white hover:bg-amber-600'}`}
              >
                Sign Up
              </Link>
            </>
          ) : (
            <button
              type="button"
              onClick={onLogout}
              className="rounded-lg border border-slate-200 px-3 py-2 text-sm font-semibold text-slate-700 hover:bg-slate-50"
            >
              Logout
            </button>
          )}
        </nav>
      </div>
    </header>
  )
}

export default Navbar
