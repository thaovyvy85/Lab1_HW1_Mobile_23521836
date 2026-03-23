import http from '../utils/http'
import { SuccessResponse } from '../types/response'
import { Event, GetEventsResponse } from '../types/event'

const eventsApi = {
  getEvents: (limit = 20, page = 1) => {
    return http.get<SuccessResponse<GetEventsResponse>>('/events', { params: { limit, page } })
  },
  getEventDetails: (eventId: string) => {
    return http.get<SuccessResponse<Event>>(`/events/${eventId}`)
  }
}

export default eventsApi
