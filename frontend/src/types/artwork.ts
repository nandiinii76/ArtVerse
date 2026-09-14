export type ArtworkStatus = 'DRAFT' | 'PUBLISHED' | 'ARCHIVED' | 'SOLD'

export interface Artwork {
  id: string
  title: string
  description?: string
  imageUrl?: string
  artistId: string
  category: string
  style?: string
  medium?: string
  yearCreated?: number
  price?: number
  currency: string
  status: ArtworkStatus
  featured: boolean
  views: number
  createdAt: string
}

export interface ArtworkPage {
  content: Artwork[]
  totalElements: number
  totalPages: number
  number: number
  size: number
}
