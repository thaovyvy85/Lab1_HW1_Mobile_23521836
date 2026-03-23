import http from '../utils/http'
import { AuthResponse, GetMeResponse } from '../types/auth'
import { SuccessResponse } from '../types/response'
import { GetMyTicketsResponse } from '../types/ticket'

const usersApi = {
  loginAccount: (body: { email: string; password: string }) => {
    return http.post<AuthResponse>('/users/login', body)
  },
  getMe: () => {
    return http.get<GetMeResponse>('/users/me')
  },
  logout: (refreshToken: string) => {
    return http.post<SuccessResponse<{ message: string }>>('/users/logout', {
      refresh_token: refreshToken
    })
  },
  getMyTickets: (limit = 20, page = 1) => {
    return http.get<SuccessResponse<GetMyTicketsResponse>>('/users/me/tickets', {
      params: { limit, page }
    })
  }
}

export default usersApi
