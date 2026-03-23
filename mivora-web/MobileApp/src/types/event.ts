export type EventStatus = 'draft' | 'published' | 'canceled'

export interface Event {
  id: string
  organizer_id: string
  organizer_name: string
  title: string
  description?: string
  poster_url?: string
  location_text: string
  start_at: string
  end_at: string
  price_cents: number
  capacity: number
  status: EventStatus
  revenue_cents: number
}

export interface GetEventsResponse {
  events: Event[]
  limit: number
  page: number
  total_page: number
}
