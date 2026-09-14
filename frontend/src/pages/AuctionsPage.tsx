import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { api } from '@/lib/api'

type Auction = { id:string; artworkId:string; sellerId:string; startingPrice:number; currentPrice:number; minimumIncrement:number; currency:string; startsAt:string; endsAt:string; status:string; winnerId?:string }
type Page<T> = { content:T[]; totalElements:number; totalPages:number }

export function AuctionsPage() {
  const [auctions,setAuctions]=useState<Auction[]>([])
  const [loading,setLoading]=useState(true)
  useEffect(()=>{ api.get<Page<Auction>>('/auctions',{params:{size:24}}).then(r=>setAuctions(r.data.content)).catch(()=>setAuctions([])).finally(()=>setLoading(false)) },[])
  return <main>
    <section className="border-b border-ink/10 bg-ivory px-6 py-16 sm:px-10"><div className="mx-auto max-w-6xl"><p className="label-meta">The auction rooms</p><h1 className="mt-3 text-5xl sm:text-7xl">Works with a second life.</h1><p className="mt-5 max-w-2xl text-ink/65">Follow live bidding, discover the current highest offer, and enter the room when the moment feels right.</p></div></section>
    <section className="mx-auto max-w-6xl px-6 py-12 sm:px-10">{loading?<p className="py-20 text-center text-ink/50">Opening the auction rooms...</p>:auctions.length===0?<div className="border border-dashed border-ink/20 py-24 text-center"><p className="font-serif text-3xl">No live auctions yet.</p><p className="mt-3 text-ink/50">The next auction will appear here when an artist opens the room.</p></div>:<div className="grid gap-8 sm:grid-cols-2 lg:grid-cols-3">{auctions.map(a=><Link key={a.id} to={`/auctions/${a.id}`} className="museum-card p-6"><p className="label-meta">{a.status} · {new Date(a.endsAt).toLocaleDateString()}</p><h2 className="mt-3 font-serif text-3xl">Auction room</h2><p className="mt-6 text-sm text-ink/50">Current offer</p><p className="mt-1 font-serif text-2xl">{a.currency} {a.currentPrice.toLocaleString()}</p><p className="mt-4 text-xs text-ink/45">Minimum next bid: {a.currency} {(a.currentPrice+a.minimumIncrement).toLocaleString()}</p></Link>)}</div>}</section>
  </main>
}
