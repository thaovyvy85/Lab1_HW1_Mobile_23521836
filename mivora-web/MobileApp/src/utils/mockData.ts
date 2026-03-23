import { User } from '../types/user'
import { Event } from '../types/event'
import { TicketApi } from '../types/ticket'

export const MOCK_USER: User = {
  id: '1',
  name: 'Demo User',
  email: 'demo@mivora.app',
  avatar_url: 'https://via.placeholder.com/100',
  verified: 'verified',
  role: ['attendee', 'organizer']
}

export const MOCK_EVENTS: Event[] = [
  {
    id: '1',
    organizer_id: '10',
    organizer_name: 'Tech Events Co.',
    title: 'Neon Nights: Live DJ Set',
    description: 'Experience the ultimate electronic music night with top DJs.',
    poster_url: 'https://via.placeholder.com/200',
    location_text: 'District 1, HCMC',
    start_at: new Date(Date.now() + 7 * 24 * 60 * 60 * 1000).toISOString(),
    end_at: new Date(Date.now() + 7 * 24 * 60 * 60 * 1000 + 5 * 60 * 60 * 1000).toISOString(),
    price_cents: 30000000,
    capacity: 500,
    status: 'published',
    revenue_cents: 0
  },
  {
    id: '2',
    organizer_id: '11',
    organizer_name: 'Creative Studio',
    title: 'Indie Sunset Sessions',
    description: 'Cozy indie music evening by the rooftop.',
    location_text: 'District 2, HCMC',
    start_at: new Date(Date.now() + 3 * 24 * 60 * 60 * 1000).toISOString(),
    end_at: new Date(Date.now() + 3 * 24 * 60 * 60 * 1000 + 4 * 60 * 60 * 1000).toISOString(),
    price_cents: 15000000,
    capacity: 200,
    status: 'published',
    revenue_cents: 0
  },
  {
    id: '3',
    organizer_id: '12',
    organizer_name: 'Startup Hub',
    title: 'Startup Night: Pitches & Beers',
    description: 'Network with founders and pitch your idea.',
    location_text: 'District 7, HCMC',
    start_at: new Date(Date.now() + 14 * 24 * 60 * 60 * 1000).toISOString(),
    end_at: new Date(Date.now() + 14 * 24 * 60 * 60 * 1000 + 3 * 60 * 60 * 1000).toISOString(),
    price_cents: 10000000,
    capacity: 300,
    status: 'published',
    revenue_cents: 0
  },
  {
    id: '4',
    organizer_id: '13',
    organizer_name: 'Art Collective',
    title: 'Art & Chill',
    description: 'Browse local art, enjoy drinks, and relax.',
    location_text: 'District 3, HCMC',
    start_at: new Date(Date.now() + 5 * 24 * 60 * 60 * 1000).toISOString(),
    end_at: new Date(Date.now() + 5 * 24 * 60 * 60 * 1000 + 6 * 60 * 60 * 1000).toISOString(),
    price_cents: 20000000,
    capacity: 150,
    status: 'published',
    revenue_cents: 0
  }
]

export const MOCK_TICKETS: TicketApi[] = [
  {
    id: 'ticket-1',
    event_title: 'Neon Nights: Live DJ Set',
    event_status: 'published',
    ticket_status: 'booked',
    checked_in_at: null,
    price_cents: 30000000,
    qr_code: 'QR_CODE_MOCK_1',
    event_id: '1'
  },
  {
    id: 'ticket-2',
    event_title: 'Indie Sunset Sessions',
    event_status: 'published',
    ticket_status: 'checked_in',
    checked_in_at: new Date(Date.now() - 2 * 24 * 60 * 60 * 1000).toISOString(),
    price_cents: 15000000,
    qr_code: 'QR_CODE_MOCK_2',
    event_id: '2'
  },
  {
    id: 'ticket-3',
    event_title: 'Art & Chill',
    event_status: 'published',
    ticket_status: 'booked',
    checked_in_at: null,
    price_cents: 20000000,
    qr_code: 'QR_CODE_MOCK_3',
    event_id: '4'
  }
]
