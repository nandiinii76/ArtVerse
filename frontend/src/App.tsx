import { Route, Routes } from 'react-router-dom'
import { Header } from '@/components/layout/Header'
import { HomePage } from '@/pages/HomePage'
import { LoginPage } from '@/pages/LoginPage'
import { RegisterPage } from '@/pages/RegisterPage'
import { DashboardPage } from '@/pages/DashboardPage'
import { ExplorePage } from '@/pages/ExplorePage'
import { ArtworkPage } from '@/pages/ArtworkPage'
import { StudioPage } from '@/pages/StudioPage'
import { MuseumPage } from '@/pages/MuseumPage'
import { CuratorPage } from '@/pages/CuratorPage'
import { AuctionsPage } from '@/pages/AuctionsPage'
import { AuctionDetailPage } from '@/pages/AuctionDetailPage'
import { CollectionsPage } from '@/pages/CollectionsPage'
import { NotificationsPage } from '@/pages/NotificationsPage'
import { MarketplacePage } from '@/pages/MarketplacePage'
import { ProtectedRoute } from '@/routes/ProtectedRoute'

export default function App() {
  return <div className="min-h-screen bg-paper text-ink"><Header /><Routes>
    <Route path="/" element={<HomePage />} />
    <Route path="/explore" element={<ExplorePage />} />
    <Route path="/museum" element={<MuseumPage />} />
    <Route path="/curator" element={<CuratorPage />} />
    <Route path="/auctions" element={<AuctionsPage />} />
    <Route path="/auctions/:id" element={<AuctionDetailPage />} />
    <Route path="/marketplace" element={<MarketplacePage />} />
    <Route path="/artworks/:id" element={<ArtworkPage />} />
    <Route path="/login" element={<LoginPage />} />
    <Route path="/register" element={<RegisterPage />} />
    <Route element={<ProtectedRoute />}>
      <Route path="/dashboard" element={<DashboardPage />} />
      <Route path="/studio" element={<StudioPage />} />
      <Route path="/collections" element={<CollectionsPage />} />
      <Route path="/notifications" element={<NotificationsPage />} />
    </Route>
    <Route path="*" element={<NotFound />} />
  </Routes></div>
}
function NotFound() { return <main className="mx-auto max-w-3xl px-6 py-32 text-center"><p className="label-meta mb-4">404</p><h1 className="font-serif text-4xl">The artwork isn't here.</h1><p className="mt-4 text-ink/60">There is plenty more to discover.</p></main> }
