import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { api, extractErrorMessage } from '@/lib/api'
import { connectNotifications } from '@/lib/realtime'
import { useAuthStore } from '@/store/authStore'

type Notification = { id: string; type: string; title: string; message: string; readAt?: string; createdAt: string }
type UnreadResponse = { count: number }

export function NotificationBell() {
  const user = useAuthStore(s => s.user)
  const [open, setOpen] = useState(false)
  const [count, setCount] = useState(0)
  const [items, setItems] = useState<Notification[]>([])
  const [error, setError] = useState('')

  async function refreshCount() {
    try {
      const response = await api.get<UnreadResponse>('/notifications/unread-count')
      setCount(response.data.count ?? 0)
    } catch { /* notifications are non-critical */ }
  }

  async function openNotifications() {
    const next = !open
    setOpen(next)
    if (!next) return
    setError('')
    try {
      const response = await api.get('/notifications', { params: { page: 0, size: 12 } })
      setItems(response.data.content ?? [])
      await refreshCount()
    } catch (e) { setError(extractErrorMessage(e)) }
  }

  useEffect(() => {
    void refreshCount()
    if (!user?.id) return
    return connectNotifications(user.id, (incoming) => {
      const notification = incoming as Notification
      if (!notification?.id) return
      setItems(current => [notification, ...current.filter(item => item.id !== notification.id)].slice(0, 12))
      setCount(current => current + (notification.readAt ? 0 : 1))
    })
  }, [user?.id])

  async function markRead(id: string) {
    try {
      const item = items.find(value => value.id === id)
      await api.patch(`/notifications/${id}/read`)
      setItems(current => current.map(value => value.id === id ? { ...value, readAt: new Date().toISOString() } : value))
      if (item && !item.readAt) setCount(current => Math.max(0, current - 1))
    } catch (e) { setError(extractErrorMessage(e)) }
  }

  async function markAllRead() {
    try {
      await api.patch('/notifications/read-all')
      setItems(current => current.map(item => ({ ...item, readAt: item.readAt ?? new Date().toISOString() })))
      setCount(0)
    } catch (e) { setError(extractErrorMessage(e)) }
  }

  return <div className="relative">
    <button aria-label="Notifications" aria-expanded={open} onClick={() => void openNotifications()} className="relative p-2 text-ink/65 hover:text-oxblood">
      <span className="text-lg">♢</span>
      {count > 0 && <span className="absolute -right-1 -top-1 min-w-5 rounded-full bg-oxblood px-1.5 py-0.5 text-[9px] text-paper">{count > 99 ? '99+' : count}</span>}
    </button>
    {open && <div className="absolute right-0 top-12 z-30 w-[min(22rem,calc(100vw-2rem))] border border-ink/15 bg-paper p-4 shadow-[0_18px_55px_rgba(29,27,24,.14)]">
      <div className="flex items-center justify-between border-b border-ink/10 pb-3"><div><p className="label-meta">Correspondence</p><h2 className="font-serif text-xl">Notifications</h2></div>{count > 0 && <button onClick={() => void markAllRead()} className="text-[9px] uppercase tracking-wider text-oxblood">Read all</button>}</div>
      {error && <p className="mt-3 text-xs text-oxblood">{error}</p>}
      <div className="mt-2 max-h-80 overflow-auto">{items.length === 0 ? <p className="py-8 text-center text-sm text-ink/45">No correspondence.</p> : items.map(item => <button key={item.id} onClick={() => !item.readAt && void markRead(item.id)} className={`block w-full border-b border-ink/10 px-2 py-4 text-left ${item.readAt ? 'opacity-55' : 'bg-ink/[.025]'}`}><p className="text-[9px] uppercase tracking-wider text-olive">{item.type.replaceAll('_', ' ')}</p><p className="mt-1 font-serif text-base">{item.title}</p><p className="mt-1 text-xs leading-5 text-ink/60">{item.message}</p><p className="mt-2 text-[9px] text-ink/35">{new Date(item.createdAt).toLocaleString()}</p></button>)}</div>
      <Link to="/notifications" onClick={() => setOpen(false)} className="mt-3 block text-center text-[9px] uppercase tracking-[0.14em] text-oxblood">View all notifications →</Link>
    </div>}
  </div>
}
