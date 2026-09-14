import { api } from './api'
import type { Artwork, ArtworkPage } from '@/types/artwork'

export async function listArtworks(params: { q?: string; category?: string; page?: number; size?: number; sort?: string }) {
  const { data } = await api.get<ArtworkPage>('/artworks', { params })
  return data
}

export async function getArtwork(id: string) {
  const { data } = await api.get<Artwork>(`/artworks/${id}`)
  return data
}

export async function createArtwork(payload: Partial<Artwork>) {
  const { data } = await api.post<Artwork>('/artworks', payload)
  return data
}
