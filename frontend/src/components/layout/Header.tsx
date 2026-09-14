import { Link, useNavigate } from 'react-router-dom'
import { useAuthStore } from '@/store/authStore'

export function Header() {
  const navigate = useNavigate(); const user = useAuthStore(s=>s.user); const isAuthenticated=useAuthStore(s=>s.isAuthenticated); const logout=useAuthStore(s=>s.logout)
  return <header className="sticky top-0 z-20 border-b border-ink/10 bg-paper/95 backdrop-blur-sm"><div className="mx-auto flex max-w-7xl items-center justify-between px-6 py-5 sm:px-10">
    <Link to="/" className="font-serif text-2xl tracking-[0.18em]">ARTVERSE</Link>
    <nav className="hidden items-center gap-7 text-[11px] uppercase tracking-[0.18em] text-ink/65 md:flex">
      <Link to="/explore" className="hover:text-oxblood">Explore</Link>
      <Link to="/curator" className="hover:text-oxblood">Curator</Link>
      <Link to="/museum" className="hover:text-oxblood">Museum</Link>
      <Link to="/auctions" className="hover:text-oxblood">Auctions</Link>
      <span>Artists</span><span>Exhibitions</span>
    </nav>
    <div className="flex items-center gap-5 text-sm">{isAuthenticated?<><Link to="/studio" className="hidden text-ink/65 hover:text-oxblood sm:block">Studio</Link><span className="hidden text-ink/60 lg:inline">{user?.displayName}</span><button onClick={()=>{logout();navigate('/')}} className="btn-outline">Sign out</button></>:<><Link to="/login" className="text-ink/70 hover:text-oxblood">Sign in</Link><Link to="/register" className="btn-outline">Join ARTVERSE</Link></>}</div>
  </div></header>
}
