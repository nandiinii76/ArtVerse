import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { api, extractErrorMessage } from '@/lib/api'

type Listing = { id: string; artworkId: string; sellerId: string; price: number; currency: string; status: string }
type Page<T> = { content?: T[] }
type Order = { id: string; artworkId: string; amount: number; currency: string; status: string; paymentReference?: string; createdAt: string }

export function MarketplacePage() {
  const [listings, setListings] = useState<Listing[]>([])
  const [orders, setOrders] = useState<Order[]>([])
  const [loading, setLoading] = useState(true)
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')
  const [paying, setPaying] = useState('')

  async function load() {
    setLoading(true); setError('')
    try {
      const [listingResponse, orderResponse] = await Promise.all([
        api.get<Page<Listing>>('/marketplace/listings', { params: { page: 0, size: 24 } }),
        api.get<Page<Order>>('/marketplace/orders/me', { params: { page: 0, size: 12 } }),
      ])
      setListings(listingResponse.data.content ?? [])
      setOrders(orderResponse.data.content ?? [])
    } catch (e) { setError(extractErrorMessage(e)) }
    finally { setLoading(false) }
  }

  useEffect(() => { void load() }, [])

  async function buy(listingId: string) {
    setMessage(''); setError('')
    try {
      const response = await api.post<Order>(`/marketplace/orders/${listingId}`)
      setMessage(`Order ${response.data.id.slice(0, 8)} created. Complete the demo payment below.`)
      await load()
    } catch (e) { setError(extractErrorMessage(e)) }
  }

  async function pay(order: Order) {
    setPaying(order.id); setMessage(''); setError('')
    try {
      await api.post(`/marketplace/orders/${order.id}/pay`, { paymentReference: `ARTVERSE-DEMO-${Date.now()}` })
      setMessage('Payment recorded and ownership transferred successfully.')
      await load()
    } catch (e) { setError(extractErrorMessage(e)) }
    finally { setPaying('') }
  }

  return <main className="mx-auto max-w-7xl px-6 py-16 sm:px-10">
    <header className="border-b border-ink/15 pb-8"><p className="label-meta">The private market</p><h1 className="mt-3 text-5xl sm:text-6xl">Acquire a work.</h1><p className="mt-4 max-w-2xl text-sm leading-7 text-ink/55">Discover available works and manage your ARTVERSE purchases from one quiet ledger.</p></header>
    {message && <p className="mt-6 border border-olive/20 bg-olive/[.04] p-4 text-sm text-olive">{message}</p>}
    {error && <p className="mt-6 border border-oxblood/20 bg-oxblood/[.03] p-4 text-sm text-oxblood">{error}</p>}
    <section className="mt-10"><p className="label-meta">Available now</p><h2 className="mt-2 font-serif text-3xl">Works for acquisition</h2>
      {loading ? <p className="py-16 text-center text-sm text-ink/45">Opening the market ledger…</p> : listings.length === 0 ? <div className="mt-6 border border-dashed border-ink/20 py-20 text-center text-sm text-ink/45">No works are currently listed.</div> : <div className="mt-6 grid gap-6 sm:grid-cols-2 lg:grid-cols-3">{listings.map(listing => <article key={listing.id} className="museum-card p-6"><p className="label-meta">Available · {listing.currency}</p><h3 className="mt-3 font-serif text-2xl">Catalogue work</h3><p className="mt-5 font-serif text-2xl">{listing.currency} {listing.price.toLocaleString()}</p><div className="mt-6 flex gap-3"><Link to={`/artworks/${listing.artworkId}`} className="btn-outline flex-1 justify-center">View work</Link><button onClick={() => void buy(listing.id)} className="flex-1 bg-oxblood px-4 py-3 text-[10px] uppercase tracking-wider text-paper hover:bg-ink">Acquire</button></div></article>)}</div>}
    </section>
    <section className="mt-16 border-t border-ink/10 pt-10"><p className="label-meta">Your ledger</p><h2 className="mt-2 font-serif text-3xl">Orders</h2>{orders.length === 0 ? <p className="mt-6 text-sm text-ink/45">Your purchases will appear here.</p> : <div className="mt-6 divide-y divide-ink/10 border-y border-ink/10">{orders.map(order => <article key={order.id} className="flex flex-col justify-between gap-4 px-4 py-5 sm:flex-row sm:items-center"><div><p className="label-meta">{order.status}</p><p className="mt-1 font-serif text-xl">{order.currency} {order.amount.toLocaleString()}</p><p className="mt-1 text-xs text-ink/40">{new Date(order.createdAt).toLocaleString()}</p></div>{order.status === 'PENDING' && <button disabled={paying === order.id} onClick={() => void pay(order)} className="btn-outline disabled:opacity-40">{paying === order.id ? 'Recording…' : 'Complete demo payment'}</button>}{order.status === 'PAID' && <span className="text-[10px] uppercase tracking-[0.14em] text-olive">Ownership confirmed</span>}</article>)}</div>}</section>
  </main>
}
