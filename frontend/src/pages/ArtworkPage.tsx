import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { getArtwork } from '@/lib/artworks'
import type { Artwork } from '@/types/artwork'
import { AddToCollection } from '@/components/collections/AddToCollection'
import { ArtworkSocial } from '@/components/social/ArtworkSocial'

export function ArtworkPage() {
  const { id } = useParams(); const [art, setArt] = useState<Artwork | null>(null)
  useEffect(() => { if (id) getArtwork(id).then(setArt).catch(() => setArt(null)) }, [id])
  if (!art) return <main className="mx-auto max-w-4xl px-6 py-32 text-center"><p className="label-meta">Catalogue</p><h1 className="mt-3 text-4xl">Artwork not found.</h1><Link className="btn-outline mt-8" to="/explore">Return to archive</Link></main>
  return <main className="mx-auto max-w-6xl px-6 py-16 sm:px-10"><div className="grid gap-12 lg:grid-cols-[1.2fr_.8fr]"><div className="bg-ivory p-4"><div className="aspect-[4/5] overflow-hidden bg-paper">{art.imageUrl ? <img src={art.imageUrl} className="h-full w-full object-cover" alt={art.title}/> : <div className="flex h-full items-center justify-center font-serif text-5xl text-ink/15">ARTVERSE</div>}</div></div><article className="self-center"><p className="label-meta">{art.category} · {art.yearCreated ?? 'Undated'}</p><h1 className="mt-3 text-5xl">{art.title}</h1><p className="mt-7 leading-8 text-ink/70">{art.description || 'A work preserved in the ARTVERSE collection.'}</p><dl className="mt-10 space-y-4 border-y border-ink/15 py-6 text-sm"><div className="flex justify-between"><dt className="text-ink/50">Medium</dt><dd>{art.medium || '—'}</dd></div><div className="flex justify-between"><dt className="text-ink/50">Style</dt><dd>{art.style || '—'}</dd></div><div className="flex justify-between"><dt className="text-ink/50">Views</dt><dd>{art.views}</dd></div></dl>{art.price != null && <p className="mt-8 font-serif text-3xl">{art.currency} {art.price.toLocaleString()}</p>}<button className="mt-8 w-full bg-oxblood px-6 py-4 text-sm uppercase tracking-widest text-paper hover:bg-ink">Enquire about this work</button>{id && <AddToCollection artworkId={id} />}</article></div>{id && <ArtworkSocial artworkId={id} />}</main>
}
