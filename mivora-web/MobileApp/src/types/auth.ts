import { SuccessResponse } from './response'
import { User } from './user'

export type AuthResponse = SuccessResponse<{
  access_token: string
  refresh_token: string
}>

export type GetMeResponse = SuccessResponse<User>
