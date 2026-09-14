import { FormEvent, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { register } from '@/lib/auth'
import { extractErrorMessage } from '@/lib/api'
import { useAuthStore } from '@/store/authStore'

export function RegisterPage() {
  const navigate = useNavigate()
  const setSession = useAuthStore((s) => s.setSession)

  const [displayName, setDisplayName] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [isSubmitting, setIsSubmitting] = useState(false)

  async function handleSubmit(e: FormEvent) {
    e.preventDefault()
    setError(null)
    setIsSubmitting(true)
    try {
      const auth = await register({ displayName, email, password })
      setSession(auth)
      navigate('/dashboard', { replace: true })
    } catch (err) {
      setError(extractErrorMessage(err, 'Could not create your account'))
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <main className="mx-auto flex max-w-md flex-col px-6 py-24 sm:px-0">
      <p className="label-meta mb-4">Join ARTVERSE</p>
      <h1 className="font-serif text-4xl text-ink">Create an account</h1>

      <form onSubmit={handleSubmit} className="mt-10 space-y-7">
        <div>
          <label htmlFor="displayName" className="label-meta mb-2 block">
            Name
          </label>
          <input
            id="displayName"
            type="text"
            required
            maxLength={120}
            autoComplete="name"
            value={displayName}
            onChange={(e) => setDisplayName(e.target.value)}
            className="field"
          />
        </div>

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
            minLength={8}
            autoComplete="new-password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            className="field"
          />
          <p className="mt-2 text-xs text-ink/45">At least 8 characters.</p>
        </div>

        {error && <p className="text-sm text-oxblood">{error}</p>}

        <button type="submit" disabled={isSubmitting} className="btn-outline w-full justify-center disabled:opacity-50">
          {isSubmitting ? 'Creating account…' : 'Create account'}
        </button>
      </form>

      <p className="mt-8 text-sm text-ink/60">
        Already have an account?{' '}
        <Link to="/login" className="text-oxblood hover:underline">
          Sign in
        </Link>
      </p>
    </main>
  )
}
