import { FormEvent, useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { login } from '@/lib/auth'
import { extractErrorMessage } from '@/lib/api'
import { useAuthStore } from '@/store/authStore'

export function LoginPage() {
  const navigate = useNavigate()
  const location = useLocation()
  const setSession = useAuthStore((s) => s.setSession)

  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [isSubmitting, setIsSubmitting] = useState(false)

  async function handleSubmit(e: FormEvent) {
    e.preventDefault()
    setError(null)
    setIsSubmitting(true)
    try {
      const auth = await login({ email, password })
      setSession(auth)
      const from = (location.state as { from?: Location })?.from?.pathname ?? '/dashboard'
      navigate(from, { replace: true })
    } catch (err) {
      setError(extractErrorMessage(err, 'Invalid email or password'))
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <main className="mx-auto flex max-w-md flex-col px-6 py-24 sm:px-0">
      <p className="label-meta mb-4">Welcome back</p>
      <h1 className="font-serif text-4xl text-ink">Sign in</h1>

      <form onSubmit={handleSubmit} className="mt-10 space-y-7">
        <div>
          <label htmlFor="email" className="label-meta mb-2 block">
            Email
          </label>
          <input
            id="email"
            type="email"
            required
            autoComplete="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            className="field"
          />
        </div>

        <div>
          <label htmlFor="password" className="label-meta mb-2 block">
            Password
          </label>
          <input
            id="password"
            type="password"
            required
            autoComplete="current-password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            className="field"
          />
        </div>

        {error && <p className="text-sm text-oxblood">{error}</p>}

        <button type="submit" disabled={isSubmitting} className="btn-outline w-full justify-center disabled:opacity-50">
          {isSubmitting ? 'Signing in…' : 'Sign in'}
        </button>
      </form>

      <p className="mt-8 text-sm text-ink/60">
        New to ARTVERSE?{' '}
        <Link to="/register" className="text-oxblood hover:underline">
          Create an account
        </Link>
      </p>
    </main>
  )
}
