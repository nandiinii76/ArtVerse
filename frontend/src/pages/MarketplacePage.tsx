import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { api, extractErrorMessage } from '@/lib/api'

type Listing = { id: string; artworkId: string; sellerId: string; price: number; currency: string; status: string }
type Page<T> = { content?: T[] }
type Order = { id: string; buyerId: string; sellerId?: string; artworkId: string; auctionId?: string; amount: number; currency: string; status: string; paymentReference?: string; createdAt: string }

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
        api.get<Page<Order>>('/marketplace/orders/me', { params: { page: 0, size: 20 } }),
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
      setMessage(`Order ${response.data.id.slice(0, 8)} created. Complete payment below.`)
      await load()
    } catch (e) { setError(extractErrorMessage(e)) }
  }

  async function pay(order: Order) {
    setPaying(order.id); setMessage(''); setError('')
    try {
      await api.post(`/marketplace/orders/${order.id}/pay`, { paymentReference: `ARTVERSE-DEMO-${Date.now()}` })
      setMessage(order.auctionId ? 'Auction payment recorded. The artwork is now yours.' : 'Payment recorded and ownership transferred successfully.')
      await load()
    } catch (e) { setError(extractErrorMessage(e)) }
    finally { setPaying('') }
  }

  const pendingAuctionOrders = orders.filter(order => order.auctionId && order.status === 'PENDING')

  return <main className="mx-auto max-w-7xl px-6 py-16 sm:px-10">
    <header className="border-b border-ink/15 pb-8"><p className="label-meta">The private market</p><h1 className="mt-3 text-5xl sm:text-6xl">Acquire a work.</h1><p className="mt-4 max-w-2xl text-sm leading-7 text-ink/55">Discover available works, settle auction wins, and manage your ARTVERSE purchases from one quiet ledger.</p></header>
    {message && <p className="mt-6 border border-olive/20 bg-olive/[.04] p-4 text-sm text-olive">{message}</p>}
    {error && <p className="mt-6 border border-oxblood/20 bg-oxblood/[.03] p-4 text-sm text-oxblood">{error}</p>}

    {pendingAuctionOrders.length > 0 && <section className="mt-10 border border-oxblood/20 bg-oxblood/[.025] p-6 sm:p-8"><p className="label-meta">Auction settlement</p><h2 className="mt-2 font-serif text-3xl">You won an auction.</h2><p className="mt-3 max-w-2xl text-sm leading-7 text-ink/60">Your winning bids are reserved for you. Complete payment to transfer ownership of the artwork.</p><div className="mt-6 space-y-4">{pendingAuctionOrders.map(order => <article key={order.id} className="flex flex-col justify-between gap-5 border-t border-ink/10 pt-5 sm:flex-row sm:items-center"><div><p className="label-meta">Payment due · Auction</p><p className="mt-1 font-serif text-2xl">{order.currency} {order.amount.toLocaleString()}</p><p className="mt-1 text-xs text-ink/40">Order {order.id.slice(0, 8)} · {new Date(order.createdAt).toLocaleString()}</p></div><div className="flex gap-3"><Link to={`/artworks/${order.artworkId}`} className="btn-outline">View work</Link><button disabled={paying === order.id} onClick={() => void pay(order)} className="bg-oxblood px-5 py-3 text-[10px] uppercase tracking-wider text-paper hover:bg-ink disabled:opacity-40">{paying === order.id ? 'Processing…' : 'Complete payment'}</button></div></article>)}</div></section>}

    <section className="mt-10"><p className="label-meta">Available now</p><h2 className="mt-2 font-serif text-3xl">Works for acquisition</h2>
      {loading ? <p className="py-16 text-center text-sm text-ink/45">Opening the market ledger…</p> : listings.length === 0 ? <div className="mt-6 border border-dashed border-ink/20 py-20 text-center text-sm text-ink/45">No works are currently listed.</div> : <div className="mt-6 grid gap-6 sm:grid-cols-2 lg:grid-cols-3">{listings.map(listing => <article key={listing.id} className="museum-card p-6"><p className="label-meta">Available · {listing.currency}</p><h3 className="mt-3 font-serif text-2xl">Catalogue work</h3><p className="mt-5 font-serif text-2xl">{listing.currency} {listing.price.toLocaleString()}</p><div className="mt-6 flex gap-3"><Link to={`/artworks/${listing.artworkId}`} className="btn-outline flex-1 justify-center">View work</Link><button onClick={() => void buy(listing.id)} className="flex-1 bg-oxblood px-4 py-3 text-[10px] uppercase tracking-wider text-paper hover:bg-ink">Acquire</button></div></article>)}</div>}
    </section>

    <section className="mt-16 border-t border-ink/10 pt-10"><p className="label-meta">Your ledger</p><h2 className="mt-2 font-serif text-3xl">Orders</h2>{orders.length === 0 ? <p className="mt-6 text-sm text-ink/45">Your purchases will appear here.</p> : <div className="mt-6 divide-y divide-ink/10 border-y border-ink/10">{orders.map(order => <article key={order.id} className="flex flex-col justify-between gap-4 px-4 py-5 sm:flex-row sm:items-center"><div><p className="label-meta">{order.auctionId ? 'Auction purchase' : order.status}</p><p className="mt-1 font-serif text-xl">{order.currency} {order.amount.toLocaleString()}</p><p className="mt-1 text-xs text-ink/40">{new Date(order.createdAt).toLocaleString()}</p></div>{order.status === 'PENDING' && !order.auctionId && <button disabled={paying === order.id} onClick={() => void pay(order)} className="btn-outline disabled:opacity-40">{paying === order.id ? 'Recording…' : 'Complete demo payment'}</button>}{order.status === 'PAID' && <span className="text-[10px] uppercase tracking-[0.14em] text-olive">Ownership confirmed</span>}</article>)}</div>}</section>
  </main>
}
