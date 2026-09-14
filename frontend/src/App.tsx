import { Route, Routes } from 'react-router-dom'
import { Header } from '@/components/layout/Header'
import { HomePage } from '@/pages/HomePage'
import { LoginPage } from '@/pages/LoginPage'
import { RegisterPage } from '@/pages/RegisterPage'
import { DashboardPage } from '@/pages/DashboardPage'
import { ProtectedRoute } from '@/routes/ProtectedRoute'

export default function App() {
  return (
    <div className="min-h-screen bg-paper text-ink">
      <Header />
      <Routes>
        <Route path="/" element={<HomePage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />

        <Route element={<ProtectedRoute />}>
          <Route path="/dashboard" element={<DashboardPage />} />
        </Route>

        <Route path="*" element={<NotFound />} />
      </Routes>
    </div>
  )
}

function NotFound() {
  return (
    <main className="mx-auto max-w-3xl px-6 py-32 text-center">
      <p className="label-meta mb-4">404</p>
      <h1 className="font-serif text-4xl text-ink">The artwork isn't here.</h1>
      <p className="mt-4 text-ink/60">But there is plenty more to discover.</p>
    </main>
  )
}
