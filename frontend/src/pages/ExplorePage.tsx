import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { listArtworks } from '@/lib/artworks'
import type { Artwork } from '@/types/artwork'

const categories = ['All', 'Painting', 'Sculpture', 'Photography', 'Digital', 'Printmaking', 'Mixed Media']

export function ExplorePage() {
  const [artworks, setArtworks] = useState<Artwork[]>([])
  const [q, setQ] = useState('')
  const [category, setCategory] = useState('All')
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    setLoading(true)
    listArtworks({ q: q || undefined, category: category === 'All' ? undefined : category, size: 24 })
      .then((p) => setArtworks(p.content)).catch(() => setArtworks([])).finally(() => setLoading(false))
  }, [q, category])

  return <main>
    <section className="border-b border-ink/10 bg-ivory px-6 py-16 sm:px-10">
      <div className="mx-auto max-w-6xl">
        <p className="label-meta">The collection</p>
        <h1 className="mt-3 text-5xl sm:text-7xl">Explore the archive.</h1>
        <p className="mt-5 max-w-2xl text-ink/65">Discover paintings, sculpture, photography and digital works through a quiet museum-inspired catalogue.</p>
        <input value={q} onChange={(e) => setQ(e.target.value)} placeholder="Search title, style, medium..." className="mt-8 w-full max-w-xl border border-ink/20 bg-paper px-5 py-4 outline-none focus:border-oxblood" />
      </div>
    </section>
    <section className="mx-auto max-w-6xl px-6 py-10 sm:px-10">
      <div className="mb-10 flex flex-wrap gap-2">{categories.map(c => <button key={c} onClick={() => setCategory(c)} className={`px-4 py-2 text-xs uppercase tracking-widest border ${category === c ? 'border-oxblood bg-oxblood text-paper' : 'border-ink/15 hover:border-ink/40'}`}>{c}</button>)}</div>
      {loading ? <p className="py-20 text-center text-ink/50">Opening the archive...</p> : artworks.length === 0 ? <div className="border border-dashed border-ink/20 py-24 text-center"><p className="font-serif text-3xl">The gallery awaits its first works.</p><p className="mt-3 text-ink/50">Publish an artwork from the studio to see it here.</p></div> : <div className="grid gap-8 sm:grid-cols-2 lg:grid-cols-3">{artworks.map(a => <Link to={`/artworks/${a.id}`} key={a.id} className="group"><div className="aspect-[4/5] overflow-hidden bg-ivory border border-ink/10">{a.imageUrl ? <img src={a.imageUrl} alt={a.title} className="h-full w-full object-cover transition duration-700 group-hover:scale-105" /> : <div className="flex h-full items-center justify-center font-serif text-3xl text-ink/20">ARTVERSE</div>}</div><p className="label-meta mt-4">{a.category} · {a.yearCreated ?? 'Contemporary'}</p><h2 className="mt-1 text-2xl group-hover:text-oxblood">{a.title}</h2><p className="mt-1 text-sm text-ink/50">{a.medium || a.style || 'Original work'}</p></Link>)}</div>}
    </section>
  </main>
}
