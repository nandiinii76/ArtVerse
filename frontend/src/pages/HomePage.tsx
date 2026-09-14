import { Link } from 'react-router-dom'
import { useAuthStore } from '@/store/authStore'

export function HomePage() {
  const isAuthenticated = useAuthStore((s) => s.isAuthenticated)

  return (
    <main className="mx-auto max-w-6xl px-6 py-24 sm:px-10 sm:py-32">
      <p className="label-meta mb-6">Foundation build — Phase 0</p>

      <h1 className="max-w-3xl font-serif text-5xl leading-[1.05] text-ink sm:text-7xl">
        Art beyond boundaries.
      </h1>

      <p className="mt-8 max-w-md text-lg leading-relaxed text-ink/70">
        This is the working skeleton for ARTVERSE — a Java and PostgreSQL backend and a React
        frontend, connected end to end through a real authentication flow.
      </p>

      <div className="mt-10 flex flex-wrap gap-4">
        {isAuthenticated ? (
          <Link to="/dashboard" className="btn-outline">
            Go to my account
          </Link>
        ) : (
          <>
            <Link to="/register" className="btn-outline">
              Create an account
            </Link>
            <Link to="/login" className="btn-outline">
              Sign in
            </Link>
          </>
        )}
      </div>

      <div className="mt-24 border-t border-ink/10 pt-10">
        <p className="label-meta mb-3">What's wired up</p>
        <ul className="max-w-xl space-y-2 text-sm leading-relaxed text-ink/70">
          <li>Register / login / token refresh against the Spring Boot API</li>
          <li>Stateless JWT auth with BCrypt password hashing</li>
          <li>Postgres schema managed by Flyway migrations</li>
          <li>A protected dashboard route that reads the authenticated user back from the API</li>
        </ul>
      </div>
    </main>
  )
}
