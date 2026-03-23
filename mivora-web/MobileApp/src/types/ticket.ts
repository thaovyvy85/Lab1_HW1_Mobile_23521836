export interface TicketApi {
  id: string
  event_title: string
  event_status: string
  ticket_status: 'booked' | 'checked_in' | 'canceled'
  checked_in_at: string | null
  price_cents: number
  qr_code: string | null
  total_count?: string
  event_id: string
}

export interface GetMyTicketsResponse {
  tickets: TicketApi[]
  limit: number
  page: number
  total_page: number
}
