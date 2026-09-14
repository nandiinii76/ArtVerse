import { Link, useNavigate } from 'react-router-dom'
import { useAuthStore } from '@/store/authStore'

export function Header() {
  const navigate = useNavigate()
  const user = useAuthStore((s) => s.user)
  const isAuthenticated = useAuthStore((s) => s.isAuthenticated)
  const logout = useAuthStore((s) => s.logout)

  function handleLogout() {
    logout()
    navigate('/')
  }

  return (
    <header className="border-b border-ink/10 bg-paper/90 backdrop-blur-sm">
      <div className="mx-auto flex max-w-6xl items-center justify-between px-6 py-5 sm:px-10">
        <Link to="/" className="font-serif text-xl tracking-wide text-ink">
          ARTVERSE
        </Link>

        <nav className="hidden items-center gap-8 text-[12px] uppercase tracking-wideish text-ink/70 sm:flex">
          <span className="cursor-default text-ink/35">Explore</span>
          <span className="cursor-default text-ink/35">Artists</span>
          <span className="cursor-default text-ink/35">Exhibitions</span>
        </nav>

        <div className="flex items-center gap-5 text-sm">
          {isAuthenticated ? (
            <>
              <span className="hidden text-ink/70 sm:inline">{user?.displayName}</span>
              <button onClick={handleLogout} className="btn-outline">
                Sign out
              </button>
            </>
          ) : (
            <>
              <Link to="/login" className="text-ink/70 transition-colors hover:text-oxblood">
                Sign in
              </Link>
              <Link to="/register" className="btn-outline">
                Join ARTVERSE
              </Link>
            </>
          )}
        </div>
      </div>
    </header>
  )
}
