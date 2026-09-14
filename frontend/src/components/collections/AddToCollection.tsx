import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { api, extractErrorMessage } from '@/lib/api'

type Collection = { id: string; name: string; artworkCount: number }

export function AddToCollection({ artworkId }: { artworkId: string }) {
  const [collections, setCollections] = useState<Collection[]>([])
  const [open, setOpen] = useState(false)
  const [loading, setLoading] = useState(false)
  const [message, setMessage] = useState('')

  async function loadCollections() {
    setLoading(true)
    setMessage('')
    try {
      const response = await api.get('/collections', { params: { page: 0, size: 50 } })
      setCollections(response.data.content ?? [])
    } catch (e) {
      setMessage(extractErrorMessage(e))
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    if (open) void loadCollections()
  }, [open])

  async function add(collectionId: string) {
    setMessage('')
    try {
      await api.post(`/collections/${collectionId}/artworks/${artworkId}`)
      setMessage('Added to your collection.')
      setCollections(items => items.map(item => item.id === collectionId ? { ...item, artworkCount: item.artworkCount + 1 } : item))
    } catch (e) {
      setMessage(extractErrorMessage(e))
    }
  }

  return (
    <div className="relative mt-3">
      <button onClick={() => setOpen(value => !value)} className="w-full border border-ink/35 px-6 py-4 text-sm uppercase tracking-widest transition hover:border-oxblood hover:text-oxblood">
        {open ? 'Close collections' : 'Add to collection'}
      </button>
      {open && (
        <div className="mt-2 border border-ink/15 bg-paper p-4 shadow-[0_18px_50px_rgba(29,27,24,.12)]">
          {loading ? <p className="p-3 text-sm text-ink/50">Opening your archive…</p> : collections.length === 0 ? (
            <div className="p-3">
              <p className="text-sm text-ink/60">You have no collections yet.</p>
              <Link to="/collections" className="mt-4 inline-block text-[10px] uppercase tracking-[0.14em] text-oxblood hover:text-ink">Create one</Link>
            </div>
          ) : (
            <div className="space-y-1">
              {collections.map(collection => (
                <button key={collection.id} onClick={() => void add(collection.id)} className="flex w-full items-center justify-between border-b border-ink/10 px-3 py-3 text-left hover:bg-ink/[.03]">
                  <span className="font-serif text-lg">{collection.name}</span>
                  <span className="text-[10px] uppercase tracking-wider text-ink/40">{collection.artworkCount} works</span>
                </button>
              ))}
              <Link to="/collections" className="block px-3 pt-4 text-[10px] uppercase tracking-[0.14em] text-oxblood hover:text-ink">Manage collections →</Link>
            </div>
          )}
          {message && <p className="mt-3 border-t border-ink/10 pt-3 text-xs text-oxblood">{message}</p>}
        </div>
      )}
    </div>
  )
}
