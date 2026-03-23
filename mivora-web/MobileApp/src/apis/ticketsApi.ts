import http from '../utils/http'
import { SuccessResponse } from '../types/response'
import { TicketApi } from '../types/ticket'

const ticketsApi = {
  bookTicket: (eventId: string) => {
    return http.post<SuccessResponse<{ ticket: TicketApi }>>('/tickets', { event_id: eventId })
  }
}

export default ticketsApi
