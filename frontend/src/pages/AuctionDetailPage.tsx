import { FormEvent, useEffect, useMemo, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { api, extractErrorMessage } from '@/lib/api'
import { useAuthStore } from '@/store/authStore'

type Auction = { id:string; artworkId:string; sellerId:string; startingPrice:number; currentPrice:number; minimumIncrement:number; currency:string; startsAt:string; endsAt:string; status:string; winnerId?:string }
type Bid = { id:string; auctionId:string; bidderId:string; amount:number; createdAt:string }
type Page<T> = { content?:T[] }

function remaining(target:string) {
  const ms = new Date(target).getTime() - Date.now()
  if (ms <= 0) return '00:00:00'
  const total = Math.floor(ms / 1000)
  const h = Math.floor(total / 3600)
  const m = Math.floor((total % 3600) / 60)
  const s = total % 60
  return `${String(h).padStart(2,'0')}:${String(m).padStart(2,'0')}:${String(s).padStart(2,'0')}`
}

export function AuctionDetailPage() {
  const { id } = useParams()
  const user = useAuthStore(s => s.user)
  const authenticated = useAuthStore(s => s.isAuthenticated)
  const [auction,setAuction] = useState<Auction|null>(null)
  const [bids,setBids] = useState<Bid[]>([])
  const [amount,setAmount] = useState('')
  const [clock,setClock] = useState('')
  const [loading,setLoading] = useState(true)
  const [message,setMessage] = useState('')
  const [error,setError] = useState('')
  const [submitting,setSubmitting] = useState(false)

  async function load() {
    if (!id) return
    try {
      const [auctionResponse,bidsResponse] = await Promise.all([
        api.get<Auction>(`/auctions/${id}`),
        api.get<Page<Bid>>(`/auctions/${id}/bids`, { params:{page:0,size:30} })
      ])
      setAuction(auctionResponse.data)
      setBids(bidsResponse.data.content ?? [])
    } catch (e) { setError(extractErrorMessage(e)) }
    finally { setLoading(false) }
  }

  useEffect(() => { void load() }, [id])
  useEffect(() => {
    if (!auction) return
    const update = () => setClock(auction.status === 'SCHEDULED' ? remaining(auction.startsAt) : remaining(auction.endsAt))
    update()
    const timer = window.setInterval(update,1000)
    return () => window.clearInterval(timer)
  }, [auction])

  const minimum = useMemo(() => auction ? auction.currentPrice + auction.minimumIncrement : 0, [auction])

  async function placeBid(event:FormEvent) {
    event.preventDefault()
    if (!id || !auction) return
    const value = Number(amount)
    if (!Number.isFinite(value) || value < minimum) { setError(`Your bid must be at least ${auction.currency} ${minimum.toLocaleString()}.`); return }
    setSubmitting(true); setError(''); setMessage('')
    try {
      await api.post(`/auctions/${id}/bids`, { amount:value })
      setAmount('')
      setMessage('Your bid has been entered into the auction ledger.')
      await load()
    } catch (e) { setError(extractErrorMessage(e)) }
    finally { setSubmitting(false) }
  }

  if (loading) return <main className="mx-auto max-w-5xl px-6 py-32 text-center"><p className="label-meta">Auction room</p><p className="mt-4 text-ink/50">Opening the ledger…</p></main>
  if (!auction) return <main className="mx-auto max-w-5xl px-6 py-32 text-center"><p className="label-meta">Auction room</p><h1 className="mt-3 font-serif text-4xl">Room not found.</h1><Link to="/auctions" className="btn-outline mt-8">Return to auctions</Link></main>

  return <main className="mx-auto max-w-6xl px-6 py-14 sm:px-10">
    <Link to="/auctions" className="text-[10px] uppercase tracking-[0.16em] text-ink/45 hover:text-oxblood">← Auction rooms</Link>
    <div className="mt-8 grid gap-10 lg:grid-cols-[1.15fr_.85fr]">
      <section className="museum-card bg-ivory p-8 sm:p-12">
        <p className="label-meta">Private auction · {auction.status}</p>
        <h1 className="mt-4 font-serif text-5xl sm:text-6xl">Auction room</h1>
        <p className="mt-5 text-sm leading-7 text-ink/60">Artwork catalogue reference: <span className="font-mono text-xs">{auction.artworkId}</span></p>
        <div className="mt-12 border-y border-ink/15 py-8 text-center">
          <p className="label-meta">Current offer</p>
          <p className="mt-2 font-serif text-5xl">{auction.currency} {auction.currentPrice.toLocaleString()}</p>
          <p className="mt-4 text-xs text-ink/45">Next acceptable bid · {auction.currency} {minimum.toLocaleString()}</p>
        </div>
        <div className="mt-8 flex items-center justify-between text-sm"><span className="text-ink/45">{auction.status === 'SCHEDULED' ? 'Opens in' : 'Closes in'}</span><span className="font-mono tracking-wider">{clock}</span></div>
      </section>

      <aside className="border border-ink/15 p-7 sm:p-9">
        <p className="label-meta">Enter the room</p>
        {!authenticated ? <div className="mt-8"><p className="font-serif text-2xl">Sign in to bid.</p><p className="mt-3 text-sm leading-6 text-ink/55">Your bids are recorded against your ARTVERSE account.</p><Link to="/login" className="btn-outline mt-7">Sign in</Link></div> : auction.status !== 'LIVE' ? <div className="mt-8"><p className="font-serif text-2xl">Bidding is closed.</p><p className="mt-3 text-sm text-ink/55">This room is currently {auction.status.toLowerCase()}.</p></div> : auction.sellerId === user?.id ? <div className="mt-8"><p className="font-serif text-2xl">You are the host.</p><p className="mt-3 text-sm text-ink/55">The seller cannot bid on their own artwork.</p></div> : <form onSubmit={placeBid} className="mt-8"><label className="label-meta">Your offer</label><div className="mt-3 flex items-center border-b border-ink/30"><span className="font-serif text-xl">{auction.currency}</span><input className="w-full bg-transparent px-3 py-3 text-2xl outline-none" type="number" min={minimum} step="0.01" value={amount} onChange={e=>setAmount(e.target.value)} placeholder={minimum.toString()} /></div><button disabled={submitting} className="mt-7 w-full bg-oxblood px-6 py-4 text-[10px] uppercase tracking-[0.16em] text-paper hover:bg-ink disabled:opacity-50">{submitting ? 'Entering bid…' : 'Place bid'}</button></form>}
        {message && <p className="mt-5 text-sm text-olive">{message}</p>}
        {error && <p className="mt-5 text-sm text-oxblood">{error}</p>}
      </aside>
    </div>

    <section className="mt-12 border-t border-ink/10 pt-10"><div className="flex items-end justify-between"><div><p className="label-meta">The ledger</p><h2 className="mt-2 font-serif text-3xl">Recent offers</h2></div><span className="text-xs text-ink/40">{bids.length} shown</span></div>{bids.length === 0 ? <p className="mt-7 text-sm text-ink/45">No bids have been recorded yet.</p> : <div className="mt-6 divide-y divide-ink/10 border-y border-ink/10">{bids.map((bid,index)=><div key={bid.id} className="flex items-center justify-between px-4 py-4"><div><span className="label-meta">#{index+1}</span><p className="mt-1 font-mono text-xs text-ink/45">{bid.bidderId.slice(0,8)}…</p></div><div className="text-right"><p className="font-serif text-xl">{auction.currency} {bid.amount.toLocaleString()}</p><p className="mt-1 text-[10px] text-ink/35">{new Date(bid.createdAt).toLocaleString()}</p></div></div>)}</div>}</section>
  </main>
}
