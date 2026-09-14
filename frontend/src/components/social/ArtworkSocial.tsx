import { FormEvent, useEffect, useState } from 'react'
import { api, extractErrorMessage } from '@/lib/api'

type Comment = { id: string; userId: string; body: string; createdAt: string }

export function ArtworkSocial({ artworkId }: { artworkId: string }) {
  const [favorite, setFavorite] = useState(false)
  const [comments, setComments] = useState<Comment[]>([])
  const [body, setBody] = useState('')
  const [loading, setLoading] = useState(true)
  const [posting, setPosting] = useState(false)
  const [error, setError] = useState('')

  async function load() {
    setLoading(true)
    try {
      const [fav, commentPage] = await Promise.all([
        api.get<boolean>(`/social/favorites/${artworkId}`),
        api.get(`/social/comments/artwork/${artworkId}`, { params: { page: 0 } }),
      ])
      setFavorite(fav.data)
      setComments(commentPage.data.content ?? [])
    } catch (e) {
      setError(extractErrorMessage(e))
    } finally { setLoading(false) }
  }

  useEffect(() => { void load() }, [artworkId])

  async function toggleFavorite() {
    try {
      if (favorite) await api.delete(`/social/favorites/${artworkId}`)
      else await api.post(`/social/favorites/${artworkId}`)
      setFavorite(!favorite)
    } catch (e) { setError(extractErrorMessage(e)) }
  }

  async function comment(event: FormEvent) {
    event.preventDefault()
    if (!body.trim()) return
    setPosting(true); setError('')
    try {
      const response = await api.post(`/social/comments/artwork/${artworkId}`, { body: body.trim() })
      setComments(current => [response.data, ...current])
      setBody('')
    } catch (e) { setError(extractErrorMessage(e)) }
    finally { setPosting(false) }
  }

  async function removeComment(id: string) {
    try {
      await api.delete(`/social/comments/${id}`)
      setComments(current => current.filter(comment => comment.id !== id))
    } catch (e) { setError(extractErrorMessage(e)) }
  }

  return <section className="mt-14 border-t border-ink/15 pt-10">
    <div className="flex items-center justify-between">
      <p className="label-meta">Public conversation</p>
      <button onClick={() => void toggleFavorite()} disabled={loading} className={`border px-5 py-2.5 text-[10px] uppercase tracking-[0.14em] transition ${favorite ? 'border-oxblood text-oxblood' : 'border-ink/30 hover:border-oxblood hover:text-oxblood'}`}>
        {favorite ? '♥ Saved' : '♡ Save artwork'}
      </button>
    </div>
    {error && <p className="mt-4 text-sm text-oxblood">{error}</p>}
    <form onSubmit={comment} className="mt-7 flex gap-3 border-b border-ink/25 pb-3">
      <input className="field" value={body} onChange={e => setBody(e.target.value)} maxLength={2000} placeholder="Leave a thoughtful note about this work…" />
      <button className="btn-outline shrink-0" disabled={posting}>{posting ? 'Posting…' : 'Comment'}</button>
    </form>
    <div className="mt-8 space-y-6">
      {loading ? <p className="text-sm text-ink/50">Opening the conversation…</p> : comments.length === 0 ? <p className="text-sm text-ink/50">Be the first to leave a note.</p> : comments.map(item => <article key={item.id} className="border-b border-ink/10 pb-5"><div className="flex justify-between gap-4"><span className="text-[10px] uppercase tracking-[0.12em] text-ink/40">Visitor · {new Date(item.createdAt).toLocaleDateString()}</span><button onClick={() => void removeComment(item.id)} className="text-[10px] uppercase tracking-[0.12em] text-ink/35 hover:text-oxblood">Remove</button></div><p className="mt-3 leading-7 text-ink/70">{item.body}</p></article>)}
    </div>
  </section>
}
