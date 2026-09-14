import { useEffect, useState } from 'react'
import { api, extractErrorMessage } from '@/lib/api'

type Notification = { id: string; type: string; title: string; message: string; readAt?: string; createdAt: string }
type PageResponse = { content?: Notification[]; totalPages?: number; number?: number }

export function NotificationsPage() {
  const [items, setItems] = useState<Notification[]>([])
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  async function load(targetPage = page) {
    setLoading(true); setError('')
    try {
      const response = await api.get<PageResponse>('/notifications', { params: { page: targetPage, size: 20 } })
      setItems(response.data.content ?? [])
      setPage(response.data.number ?? targetPage)
      setTotalPages(response.data.totalPages ?? 0)
    } catch (e) { setError(extractErrorMessage(e)) }
    finally { setLoading(false) }
  }

  useEffect(() => { void load(0) }, [])

  async function markRead(id: string) {
    try {
      await api.patch(`/notifications/${id}/read`)
      setItems(current => current.map(item => item.id === id ? { ...item, readAt: new Date().toISOString() } : item))
    } catch (e) { setError(extractErrorMessage(e)) }
  }

  async function markAllRead() {
    try {
      await api.patch('/notifications/read-all')
      setItems(current => current.map(item => ({ ...item, readAt: item.readAt ?? new Date().toISOString() })))
    } catch (e) { setError(extractErrorMessage(e)) }
  }

  const unread = items.filter(item => !item.readAt).length

  return <main className="mx-auto max-w-5xl px-6 py-16 sm:px-10">
    <header className="border-b border-ink/15 pb-8">
      <p className="label-meta">Private correspondence</p>
      <div className="mt-3 flex flex-col justify-between gap-5 sm:flex-row sm:items-end">
        <div>
          <h1 className="font-serif text-4xl sm:text-5xl">Notifications</h1>
          <p className="mt-3 max-w-2xl text-sm leading-7 text-ink/55">Updates about your artworks, followers, conversations, auctions and marketplace activity.</p>
        </div>
        {unread > 0 && <button onClick={() => void markAllRead()} className="btn-outline shrink-0">Mark all as read</button>}
      </div>
    </header>

    {error && <p className="mt-6 border border-oxblood/20 bg-oxblood/[.03] p-4 text-sm text-oxblood">{error}</p>}

    <section className="mt-8">
      {loading ? <p className="py-16 text-center text-sm text-ink/45">Opening the correspondence ledger…</p> : items.length === 0 ?
        <div className="border border-ink/10 py-20 text-center"><p className="label-meta">All quiet</p><h2 className="mt-3 font-serif text-2xl">No notifications yet</h2><p className="mx-auto mt-3 max-w-md text-sm leading-6 text-ink/50">New followers, artwork comments, favorites, bids and marketplace orders will appear here.</p></div> :
        <div className="divide-y divide-ink/10 border-y border-ink/10">
          {items.map(item => <article key={item.id} className={`px-4 py-6 transition sm:px-6 ${item.readAt ? 'opacity-60' : 'bg-ink/[.025]'}`}>
            <div className="flex flex-col justify-between gap-3 sm:flex-row sm:items-start">
              <div>
                <div className="flex flex-wrap items-center gap-3"><span className="label-meta">{item.type.replaceAll('_', ' ')}</span>{!item.readAt && <span className="h-1.5 w-1.5 rounded-full bg-oxblood" aria-label="Unread" />}</div>
                <h2 className="mt-2 font-serif text-xl">{item.title}</h2>
                <p className="mt-2 max-w-3xl text-sm leading-7 text-ink/60">{item.message}</p>
                <p className="mt-3 text-[10px] uppercase tracking-[0.12em] text-ink/35">{new Date(item.createdAt).toLocaleString()}</p>
              </div>
              {!item.readAt && <button onClick={() => void markRead(item.id)} className="text-left text-[10px] uppercase tracking-[0.14em] text-oxblood hover:underline">Mark read</button>}
            </div>
          </article>)}
        </div>}
    </section>

    {!loading && totalPages > 1 && <div className="mt-8 flex items-center justify-between border-t border-ink/10 pt-6">
      <button disabled={page <= 0} onClick={() => void load(page - 1)} className="btn-outline disabled:cursor-not-allowed disabled:opacity-30">← Newer</button>
      <span className="text-[10px] uppercase tracking-[0.14em] text-ink/40">Page {page + 1} of {totalPages}</span>
      <button disabled={page >= totalPages - 1} onClick={() => void load(page + 1)} className="btn-outline disabled:cursor-not-allowed disabled:opacity-30">Older →</button>
    </div>}
  </main>
}
