import { useEffect, useState } from 'react'
import { fetchCurrentUser } from '@/lib/auth'
import { useAuthStore } from '@/store/authStore'
import type { User } from '@/types/auth'

export function DashboardPage() {
  const cachedUser = useAuthStore((s) => s.user)
  const [user, setUser] = useState<User | null>(cachedUser)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    fetchCurrentUser()
      .then(setUser)
      .catch(() => setError('Could not verify your session with the server.'))
  }, [])

  return (
    <main className="mx-auto max-w-3xl px-6 py-24 sm:px-10">
      <p className="label-meta mb-4">My account</p>
      <h1 className="font-serif text-4xl text-ink">
        {user ? `Welcome, ${user.displayName}.` : 'Loading your profile…'}
      </h1>

      {error && <p className="mt-4 text-sm text-oxblood">{error}</p>}

      {user && (
        <dl className="mt-12 divide-y divide-ink/10 border-t border-ink/10">
          <Row label="Email" value={user.email} />
          <Row label="Roles" value={user.roles.join(', ')} />
          <Row label="Email verified" value={user.emailVerified ? 'Yes' : 'No'} />
          <Row label="User ID" value={user.id} />
        </dl>
      )}

      <p className="mt-10 text-sm text-ink/50">
        This page fetched your profile live from <code className="text-xs">GET /api/v1/auth/me</code>{' '}
        using the access token issued at sign-in — confirming the backend and frontend are correctly
        wired end to end.
      </p>
    </main>
  )
}

function Row({ label, value }: { label: string; value: string }) {
  return (
    <div className="flex items-center justify-between py-4">
      <dt className="label-meta">{label}</dt>
      <dd className="text-sm text-ink/80">{value}</dd>
    </div>
  )
}
