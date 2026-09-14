import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { api, extractErrorMessage } from '@/lib/api'
import { useAuthStore } from '@/store/authStore'

type Artist = {
  userId: string
  displayName: string
  biography?: string
  location?: string
  websiteUrl?: string
  profileImageUrl?: string
  verified: boolean
  joinedAt: string
  artworkCount: number
  followerCount: number
}

type Artwork = {
  id: string
  title: string
  description?: string
  imageUrl?: string
  category?: string
  medium?: string
  style?: string
  yearCreated?: number
  price?: number
  currency?: string
}

type Page<T> = { content?: T[] }
type FollowResponse = { followerId: string; followingId: string; following: boolean }

export function ArtistPage() {
  const { id } = useParams()
  const currentUser = useAuthStore((state) => state.user)
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated)
  const [artist, setArtist] = useState<Artist | null>(null)
  const [works, setWorks] = useState<Artwork[]>([])
  const [following, setFollowing] = useState(false)
  const [followLoading, setFollowLoading] = useState(false)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    if (!id) return
    setLoading(true)
    setError('')
    const requests = [
      api.get<Artist>(`/artists/${id}`),
      api.get<Page<Artwork>>(`/artists/${id}/artworks`, { params: { page: 0, size: 24 } }),
    ]

    Promise.all(requests).then(([profile, portfolio]) => {
      setArtist(profile.data)
      setWorks(portfolio.data.content ?? [])
    }).catch((e) => setError(extractErrorMessage(e)))
      .finally(() => setLoading(false))
  }, [id])

  useEffect(() => {
    if (!id || !isAuthenticated || currentUser?.id === id) {
      setFollowing(false)
      return
    }
    api.get<FollowResponse>(`/social/follows/${id}`)
      .then((response) => setFollowing(response.data.following))
      .catch(() => setFollowing(false))
  }, [id, isAuthenticated, currentUser?.id])

  async function toggleFollow() {
    if (!id || !artist || !isAuthenticated || followLoading) return
    setFollowLoading(true)
    try {
      if (following) {
        await api.delete(`/social/follows/${id}`)
        setFollowing(false)
        setArtist((current) => current ? { ...current, followerCount: Math.max(0, current.followerCount - 1) } : current)
      } else {
        const response = await api.post<FollowResponse>(`/social/follows/${id}`)
        setFollowing(response.data.following)
        setArtist((current) => current ? { ...current, followerCount: current.followerCount + 1 } : current)
      }
    } catch (e) {
      setError(extractErrorMessage(e, 'Unable to update follow status.'))
    } finally {
      setFollowLoading(false)
    }
  }

  if (loading) return <main className="mx-auto max-w-6xl px-6 py-24 text-center text-ink/60">Opening the artist's gallery…</main>
  if (error || !artist) return <main className="mx-auto max-w-4xl px-6 py-24 text-center"><p className="label-meta">Gallery unavailable</p><p className="mt-3 text-ink/60">{error || 'Artist not found.'}</p></main>

  const isOwnProfile = currentUser?.id === artist.userId

  return (
    <main className="mx-auto max-w-6xl px-6 py-14">
      <section className="relative overflow-hidden border border-ink/15 bg-paper p-8 shadow-[0_20px_60px_rgba(29,27,24,.08)] md:p-12">
        <div className="absolute inset-x-0 top-0 h-1 bg-oxblood" />
        <div className="grid gap-10 md:grid-cols-[180px_1fr] md:items-center">
          <div className="mx-auto h-40 w-40 overflow-hidden rounded-full border-4 border-ink/10 bg-[#e7dcc6] shadow-inner">
            {artist.profileImageUrl ? <img src={artist.profileImageUrl} alt={artist.displayName} className="h-full w-full object-cover" /> : <div className="flex h-full items-center justify-center font-serif text-5xl text-ink/30">A</div>}
          </div>
          <div>
            <p className="label-meta">Artist Archive</p>
            <div className="mt-2 flex flex-wrap items-center gap-4">
              <h1 className="font-serif text-4xl md:text-6xl">{artist.displayName}</h1>
              {isAuthenticated && !isOwnProfile && (
                <button type="button" onClick={toggleFollow} disabled={followLoading} className="btn-outline disabled:cursor-not-allowed disabled:opacity-50">
                  {followLoading ? 'Updating…' : following ? 'Following' : 'Follow artist'}
                </button>
              )}
            </div>
            {artist.verified && <span className="mt-4 inline-block border border-olive/40 px-3 py-1 text-[10px] uppercase tracking-[.18em] text-olive">Verified Artist</span>}
            {artist.biography && <p className="mt-6 max-w-3xl text-lg leading-8 text-ink/70">{artist.biography}</p>}
            <div className="mt-7 flex flex-wrap gap-7 text-sm text-ink/60">
              <span><strong className="font-serif text-xl text-ink">{artist.artworkCount}</strong> works</span>
              <span><strong className="font-serif text-xl text-ink">{artist.followerCount}</strong> followers</span>
              {artist.location && <span>{artist.location}</span>}
            </div>
            {!isAuthenticated && <p className="mt-5 text-xs uppercase tracking-[.14em] text-ink/40">Sign in to follow this artist</p>}
            {artist.websiteUrl && <a href={artist.websiteUrl} target="_blank" rel="noreferrer" className="mt-5 inline-block text-sm text-oxblood underline underline-offset-4">Visit artist website</a>}
          </div>
        </div>
      </section>
      <section className="mt-16">
        <div className="mb-8 flex items-end justify-between border-b border-ink/15 pb-4">
          <div><p className="label-meta">Selected works</p><h2 className="mt-1 font-serif text-3xl">The Artist's Collection</h2></div>
          <span className="text-xs uppercase tracking-widest text-ink/40">{works.length} displayed</span>
        </div>
        {works.length === 0 ? <p className="py-16 text-center text-ink/50">This artist has not published any works yet.</p> :
          <div className="grid gap-8 sm:grid-cols-2 lg:grid-cols-3">
            {works.map((work) => (
              <Link key={work.id} to={`/artworks/${work.id}`} className="museum-card group overflow-hidden">
                <div className="aspect-[4/5] overflow-hidden bg-[#e7dcc6]">
                  {work.imageUrl ? <img src={work.imageUrl} alt={work.title} className="h-full w-full object-cover transition duration-700 group-hover:scale-105" /> : <div className="flex h-full items-center justify-center font-serif text-2xl text-ink/30">No image</div>}
                </div>
                <div className="p-5"><p className="label-meta">{work.category || work.medium || 'Artwork'}</p><h3 className="mt-2 font-serif text-2xl">{work.title}</h3>{work.yearCreated && <p className="mt-1 text-sm text-ink/50">{work.yearCreated}</p>}{work.price != null && <p className="mt-4 text-sm">{work.currency || 'INR'} {work.price.toLocaleString()}</p>}</div>
              </Link>
            ))}
          </div>}
      </section>
    </main>
  )
}
