import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { api, extractErrorMessage } from '@/lib/api'
import { getArtwork } from '@/lib/artworks'
import type { Artwork } from '@/types/artwork'

type Collection = { id: string; ownerId: string; name: string; description?: string; coverArtworkId?: string; publicCollection: boolean; artworkCount: number; createdAt: string }
type Item = { artworkId: string }

export function CollectionDetailPage() {
  const { id } = useParams(); const [collection, setCollection] = useState<Collection | null>(null); const [works, setWorks] = useState<Artwork[]>([]); const [error, setError] = useState('')
  useEffect(() => {
    if (!id) return
    void (async () => {
      try {
        const detail = await api.get<Collection>(`/collections/public/${id}`); setCollection(detail.data)
        const items = await api.get<{ content: Item[] }>(`/collections/public/${id}/artworks`, { params: { page: 0, size: 50 } })
        const loaded = await Promise.all((items.data.content ?? []).map(item => getArtwork(item.artworkId).catch(() => null)))
        setWorks(loaded.filter((work): work is Artwork => work !== null))
      } catch (e) { setError(extractErrorMessage(e)) }
    })()
  }, [id])

  if (error || !collection) return <main className="mx-auto max-w-4xl px-6 py-32 text-center"><p className="label-meta">Exhibition</p><h1 className="mt-3 font-serif text-4xl">{error || 'Opening the exhibition…'}</h1><Link className="btn-outline mt-8" to="/collections">Return to archive</Link></main>
  return <main className="mx-auto max-w-7xl px-6 py-14 sm:px-10">
    <header className="border-b border-ink/15 pb-12"><p className="label-meta">ARTVERSE exhibition · {collection.artworkCount} works</p><h1 className="mt-3 max-w-4xl font-serif text-5xl leading-tight sm:text-7xl">{collection.name}</h1><p className="mt-6 max-w-2xl text-base leading-8 text-ink/60">{collection.description || 'A considered gathering of works from the ARTVERSE archive.'}</p><div className="mt-6 text-[10px] uppercase tracking-[.16em] text-ink/40">{collection.publicCollection ? 'Public exhibition' : 'Private archive'}</div></header>
    {works.length === 0 ? <div className="py-24 text-center text-ink/45">This exhibition has no published works yet.</div> : <section className="mt-12 grid gap-7 sm:grid-cols-2 lg:grid-cols-3"><>{works.map((work, index) => <Link key={work.id} to={`/artworks/${work.id}`} className="group block"><div className="museum-card overflow-hidden"><div className="aspect-[4/5] bg-ivory">{work.imageUrl ? <img src={work.imageUrl} alt={work.title} className="h-full w-full object-cover transition duration-700 group-hover:scale-[1.03]"/> : <div className="flex h-full items-center justify-center font-serif text-4xl text-ink/15">{String(index + 1).padStart(2, '0')}</div>}</div><div className="p-5"><p className="label-meta">{work.category} · {work.yearCreated ?? 'Undated'}</p><h2 className="mt-2 font-serif text-2xl group-hover:text-oxblood">{work.title}</h2><p className="mt-2 text-sm text-ink/50">{work.medium || 'Mixed media'}</p></div></div></Link>)}</></section>}
  </main>
}
