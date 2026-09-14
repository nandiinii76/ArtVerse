import { FormEvent, useState } from 'react'
import { Link } from 'react-router-dom'
import { api } from '@/lib/api'

type Artwork = { id: string; title: string; imageUrl?: string; artistId: string; category: string; style?: string; price?: number; currency?: string }
type CuratorResponse = { mood: string; explanation: string; artworks: Artwork[] }

export function CuratorPage() {
  const [mood, setMood] = useState('nostalgic and calm')
  const [data, setData] = useState<CuratorResponse | null>(null)
  const [loading, setLoading] = useState(false)

  async function discover(event: FormEvent) {
    event.preventDefault()
    if (!mood.trim()) return
    setLoading(true)
    try {
      const response = await api.get<CuratorResponse>('/curator/recommendations', { params: { mood, limit: 8 } })
      setData(response.data)
    } finally { setLoading(false) }
  }

  return (
    <main className="mx-auto max-w-6xl px-6 py-16 md:px-10">
      <p className="label-meta">The Curator's Desk</p>
      <h1 className="mt-3 max-w-3xl text-5xl leading-tight md:text-7xl">Tell us how you want the gallery to feel.</h1>
      <p className="mt-6 max-w-2xl text-ink/65">ArtVerse interprets your mood and assembles a small private viewing. The recommendation layer is intentionally replaceable with embeddings or a hosted model later.</p>
      <form onSubmit={discover} className="mt-10 flex max-w-3xl gap-3 border-b border-ink/30 pb-3">
        <input className="field" value={mood} onChange={(e) => setMood(e.target.value)} placeholder="e.g. quiet monsoon evening, old library..." />
        <button className="btn-outline shrink-0" disabled={loading}>{loading ? 'Arranging…' : 'Curate'}</button>
      </form>
      {data && <section className="mt-14">
        <p className="label-meta">Private viewing · {data.mood}</p>
        <p className="mt-3 max-w-2xl text-sm text-ink/60">{data.explanation}</p>
        <div className="mt-8 grid gap-6 sm:grid-cols-2 lg:grid-cols-4">
          {data.artworks.map((art) => <Link key={art.id} to={`/artworks/${art.id}`} className="museum-card overflow-hidden">
            <div className="aspect-[4/5] bg-ink/5">{art.imageUrl ? <img src={art.imageUrl} alt={art.title} className="h-full w-full object-cover" /> : <div className="flex h-full items-center justify-center font-serif text-3xl text-ink/20">A</div>}</div>
            <div className="p-4"><h2 className="font-serif text-xl">{art.title}</h2><p className="mt-1 text-xs uppercase tracking-wider text-ink/45">{art.category} · {art.style ?? 'Original work'}</p></div>
          </Link>)}
        </div>
      </section>}
    </main>
  )
}
