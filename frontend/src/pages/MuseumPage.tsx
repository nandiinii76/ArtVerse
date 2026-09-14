import { useMemo, useState } from 'react'

const rooms = [
  { name: 'The Grand Salon', tone: 'Salon of portraits, gilded frames and quiet conversation.' },
  { name: 'The Blue Room', tone: 'A slower gallery for landscapes, water and contemplative work.' },
  { name: 'The Modern Wing', tone: 'Geometric forms, abstraction and contemporary experiments.' },
]

export function MuseumPage() {
  const [room, setRoom] = useState(0)
  const [motion, setMotion] = useState(true)
  const current = rooms[room]
  const frames = useMemo(() => Array.from({ length: 7 }, (_, i) => i), [])

  return (
    <main className="museum-page mx-auto max-w-7xl px-6 py-10 md:px-10">
      <section className="mb-10 flex flex-col justify-between gap-6 md:flex-row md:items-end">
        <div>
          <p className="label-meta">Virtual Museum · Room {room + 1}</p>
          <h1 className="mt-3 font-serif text-5xl md:text-7xl">{current.name}</h1>
          <p className="mt-4 max-w-2xl text-ink/65">{current.tone}</p>
        </div>
        <button className="museum-control" onClick={() => setMotion(!motion)}>
          {motion ? 'Pause movement' : 'Resume movement'}
        </button>
      </section>

      <div className={motion ? 'museum-stage is-moving' : 'museum-stage'}>
        <div className="museum-ceiling" />
        <div className="museum-floor" />
        <div className="museum-wall wall-back">
          {frames.map((frame) => (
            <article className={`museum-frame frame-${frame}`} key={frame}>
              <div className="museum-art" />
              <span>{['Study I', 'Nocturne', 'Still Life', 'The Orchard', 'Blue Hour', 'Figure', 'Untitled'][frame]}</span>
            </article>
          ))}
        </div>
        <div className="museum-column column-left" />
        <div className="museum-column column-right" />
        <div className="museum-bench">ARTVERSE · 2026</div>
      </div>

      <nav className="mt-8 flex flex-wrap gap-3" aria-label="Museum rooms">
        {rooms.map((item, index) => (
          <button key={item.name} className={index === room ? 'room-pill active' : 'room-pill'} onClick={() => setRoom(index)}>
            {String(index + 1).padStart(2, '0')} · {item.name}
          </button>
        ))}
      </nav>
    </main>
  )
}
