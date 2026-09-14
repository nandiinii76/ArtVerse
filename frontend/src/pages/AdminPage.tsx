import { useEffect, useState } from 'react'
import { api, extractErrorMessage } from '@/lib/api'

type Stats = { users:number; artists:number; artworks:number; marketplaceListings:number; auctions:number }
type User = { id:string; displayName:string; email:string; enabled:boolean; emailVerified:boolean; roles:string; createdAt:string }
type Artwork = { id:string; title:string; artistId:string; imageUrl?:string; category:string; status:string; createdAt:string }
type Page<T> = { content:T[]; totalPages:number; number:number }

export function AdminPage() {
  const [stats,setStats]=useState<Stats|null>(null); const [users,setUsers]=useState<User[]>([]); const [artworks,setArtworks]=useState<Artwork[]>([]); const [error,setError]=useState(''); const [loading,setLoading]=useState(true)
  async function load(){ setLoading(true); setError(''); try { const [s,u,a]=await Promise.all([api.get<Stats>('/admin/stats'),api.get<Page<User>>('/admin/users',{params:{page:0,size:12}}),api.get<Page<Artwork>>('/admin/artworks',{params:{page:0,size:12}})]); setStats(s.data); setUsers(u.data.content??[]); setArtworks(a.data.content??[]) } catch(e){setError(extractErrorMessage(e))} finally{setLoading(false)} }
  useEffect(()=>{void load()},[])
  async function toggleUser(user:User){ try { await api.patch(`/admin/users/${user.id}/enabled`,{enabled:!user.enabled}); await load() } catch(e){setError(extractErrorMessage(e))} }
  async function feature(id:string, featured:boolean){ try { await api.patch(`/admin/artworks/${id}/featured`,{featured}) ; await load() } catch(e){setError(extractErrorMessage(e))} }
  if(loading) return <main className="mx-auto max-w-7xl px-6 py-20"><p className="label-meta">Administration</p><h1 className="mt-3 font-serif text-4xl">Opening the registry…</h1></main>
  return <main className="mx-auto max-w-7xl px-6 py-14 sm:px-10">
    <div className="flex flex-col justify-between gap-5 border-b border-ink/10 pb-8 md:flex-row md:items-end"><div><p className="label-meta">Private Registry · Administration</p><h1 className="mt-3 font-serif text-5xl">The Curator's Desk</h1><p className="mt-3 max-w-2xl text-ink/60">A quiet control room for the people, artworks and activity that make ARTVERSE.</p></div><span className="border border-ink/15 px-4 py-2 text-[10px] uppercase tracking-[0.2em]">Admin only</span></div>
    {error&&<div className="mt-6 border border-oxblood/30 bg-oxblood/5 p-4 text-sm text-oxblood">{error}</div>}
    <section className="mt-8 grid gap-px border border-ink/10 bg-ink/10 sm:grid-cols-2 lg:grid-cols-5">{[['Users',stats?.users],['Artists',stats?.artists],['Artworks',stats?.artworks],['Listings',stats?.marketplaceListings],['Auctions',stats?.auctions]].map(([label,value])=><div className="bg-paper p-6" key={String(label)}><p className="label-meta">{label}</p><p className="mt-3 font-serif text-4xl">{value}</p></div>)}</section>
    <section className="mt-12 grid gap-10 lg:grid-cols-[1.1fr_.9fr]">
      <div><div className="mb-4 flex items-end justify-between"><div><p className="label-meta">01 · People</p><h2 className="mt-2 font-serif text-3xl">User registry</h2></div></div><div className="overflow-x-auto border border-ink/10"><table className="w-full text-left text-sm"><thead className="border-b border-ink/10 text-[10px] uppercase tracking-[0.16em] text-ink/50"><tr><th className="p-4">Member</th><th className="p-4">Role</th><th className="p-4">State</th><th className="p-4">Action</th></tr></thead><tbody>{users.map(u=><tr key={u.id} className="border-b border-ink/10 last:border-0"><td className="p-4"><div className="font-medium">{u.displayName}</div><div className="text-xs text-ink/50">{u.email}</div></td><td className="p-4 text-xs">{u.roles}</td><td className="p-4"><span className={u.enabled?'text-green-800':'text-oxblood'}>{u.enabled?'Enabled':'Disabled'}</span></td><td className="p-4"><button onClick={()=>toggleUser(u)} className="btn-outline text-[10px]">{u.enabled?'Disable':'Enable'}</button></td></tr>)}</tbody></table></div></div>
      <div><div className="mb-4"><p className="label-meta">02 · Collection</p><h2 className="mt-2 font-serif text-3xl">Artwork moderation</h2></div><div className="space-y-3">{artworks.map(a=><article key={a.id} className="flex gap-4 border border-ink/10 p-4">{a.imageUrl?<img src={a.imageUrl} alt="" className="h-20 w-20 object-cover"/>:<div className="h-20 w-20 bg-ink/5"/>}<div className="min-w-0 flex-1"><p className="font-serif text-lg">{a.title}</p><p className="text-xs text-ink/50">{a.category} · {a.status}</p><div className="mt-3 flex gap-2"><button onClick={()=>feature(a.id,true)} className="btn-outline text-[10px]">Feature</button><button onClick={()=>feature(a.id,false)} className="text-[10px] uppercase tracking-[0.12em] text-ink/50 hover:text-oxblood">Unfeature</button></div></div></article>)}</div></div>
    </section>
  </main>
}
