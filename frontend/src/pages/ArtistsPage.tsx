import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { api, extractErrorMessage } from '@/lib/api'

type Artist = { userId: string; displayName: string; biography?: string; location?: string; profileImageUrl?: string; verified: boolean; artworkCount: number; followerCount: number }
type Page<T> = { content?: T[]; totalPages?: number; number?: number }

export function ArtistsPage() {
  const [artists, setArtists] = useState<Artist[]>([])
  const [q, setQ] = useState('')
  const [location, setLocation] = useState('')
  const [verified, setVerified] = useState('')
  const [sort, setSort] = useState('name')
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  async function load(targetPage = 0) {
    setLoading(true); setError('')
    try {
      const response = await api.get<Page<Artist>>('/artists', { params: { q: q || undefined, location: location || undefined, verified: verified || undefined, sort, page: targetPage, size: 24 } })
      setArtists(response.data.content ?? [])
      setPage(response.data.number ?? targetPage)
      setTotalPages(response.data.totalPages ?? 0)
    } catch (e) { setError(extractErrorMessage(e)) }
    finally { setLoading(false) }
  }

  useEffect(() => { void load(0) }, [sort])

  return <main className="mx-auto max-w-7xl px-6 py-14 sm:px-10">
    <header className="border-b border-ink/15 pb-10">
      <p className="label-meta">The artist registry</p>
      <h1 className="mt-3 max-w-4xl font-serif text-5xl leading-tight sm:text-7xl">Meet the hands behind the work.</h1>
      <p className="mt-5 max-w-2xl text-base leading-7 text-ink/60">Discover painters, sculptors, photographers and digital artists represented across the ARTVERSE collection.</p>
    </header>

    <section className="mt-8 border border-ink/10 bg-paper p-5 shadow-[0_12px_40px_rgba(29,27,24,.05)]">
      <div className="grid gap-4 md:grid-cols-[1.5fr_1fr_auto_auto]">
        <input value={q} onChange={e => setQ(e.target.value)} onKeyDown={e => { if (e.key === 'Enter') void load(0) }} placeholder="Search artists or biographies…" className="field" />
        <input value={location} onChange={e => setLocation(e.target.value)} onKeyDown={e => { if (e.key === 'Enter') void load(0) }} placeholder="Location" className="field" />
        <select value={verified} onChange={e => { setVerified(e.target.value); setTimeout(() => void load(0), 0) }} className="border-b border-ink/30 bg-transparent py-3 text-sm outline-none"><option value="">All artists</option><option value="true">Verified</option><option value="false">Unverified</option></select>
        <select value={sort} onChange={e => setSort(e.target.value)} className="border-b border-ink/30 bg-transparent py-3 text-sm outline-none"><option value="name">A–Z</option><option value="followers">Most followed</option><option value="artworks">Most works</option><option value="newest">Newest</option></select>
      </div>
      <button onClick={() => void load(0)} className="btn-outline mt-5">Search registry</button>
    </section>

    {error && <p className="mt-6 border border-oxblood/20 p-4 text-sm text-oxblood">{error}</p>}
    <section className="mt-12">
      {loading ? <p className="py-20 text-center text-ink/45">Turning the pages of the registry…</p> : artists.length === 0 ? <div className="border-y border-ink/10 py-20 text-center"><p className="label-meta">No entries</p><h2 className="mt-3 font-serif text-3xl">No artists match your search.</h2></div> :
        <div className="grid gap-7 sm:grid-cols-2 lg:grid-cols-3">
          {artists.map(artist => <Link key={artist.userId} to={`/artists/${artist.userId}`} className="group border border-ink/10 bg-paper p-5 transition duration-500 hover:-translate-y-1 hover:shadow-[0_20px_50px_rgba(29,27,24,.09)]">
            <div className="flex items-center gap-5">
              <div className="h-20 w-20 shrink-0 overflow-hidden rounded-full border-2 border-ink/10 bg-[#e7dcc6]">{artist.profileImageUrl ? <img src={artist.profileImageUrl} alt={artist.displayName} className="h-full w-full object-cover transition duration-700 group-hover:scale-105" /> : <div className="flex h-full items-center justify-center font-serif text-3xl text-ink/30">A</div>}</div>
              <div className="min-w-0"><p className="label-meta">{artist.verified ? 'Verified artist' : 'Artist'}</p><h2 className="mt-1 truncate font-serif text-2xl group-hover:text-oxblood">{artist.displayName}</h2>{artist.location && <p className="mt-1 truncate text-xs text-ink/45">{artist.location}</p>}</div>
            </div>
            <p className="mt-5 line-clamp-2 text-sm leading-6 text-ink/55">{artist.biography || 'An artist represented in the ARTVERSE registry.'}</p>
            <div className="mt-5 flex gap-6 border-t border-ink/10 pt-4 text-xs text-ink/50"><span><strong className="font-serif text-lg text-ink">{artist.artworkCount}</strong> works</span><span><strong className="font-serif text-lg text-ink">{artist.followerCount}</strong> followers</span></div>
          </Link>)}
        </div>}
    </section>
    {!loading && totalPages > 1 && <div className="mt-10 flex items-center justify-between border-t border-ink/10 pt-6"><button disabled={page <= 0} onClick={() => void load(page - 1)} className="btn-outline disabled:opacity-30">← Previous</button><span className="text-[10px] uppercase tracking-[.14em] text-ink/40">Page {page + 1} of {totalPages}</span><button disabled={page >= totalPages - 1} onClick={() => void load(page + 1)} className="btn-outline disabled:opacity-30">Next →</button></div>}
  </main>
}
