import axios, { AxiosError, AxiosHeaders, AxiosInstance, HttpStatusCode, InternalAxiosRequestConfig } from 'axios'
import Constants from 'expo-constants'
import { AuthResponse, GetMeResponse } from '../types/auth'
import { clearAuthStorage, getAccessToken, getRefreshToken, setProfile, setTokens } from './storage'

interface RequestConfigWithRetry extends InternalAxiosRequestConfig {
  _retry?: boolean
}

const fallbackBaseUrl = 'https://khoinguyenpham.name.vn/api/v1'
const baseURL =
  (Constants.expoConfig?.extra?.apiBaseUrl as string | undefined) ||
  fallbackBaseUrl

class Http {
  instance: AxiosInstance
  private accessToken = ''

  constructor() {
    this.instance = axios.create({
      baseURL,
      timeout: 10000,
      headers: { 'Content-Type': 'application/json' }
    })

    this.instance.interceptors.request.use(async (config) => {
      if (!this.accessToken) {
        this.accessToken = await getAccessToken()
      }

      if (this.accessToken && config.headers) {
        config.headers.Authorization = `Bearer ${this.accessToken}`
      }

      return config
    })

    this.instance.interceptors.response.use(
      async (response) => {
        const url = response.config.url || ''

        if (url === '/users/login' || url === '/users/register') {
          const auth = response.data as AuthResponse
          this.accessToken = auth.result.access_token
          await setTokens(auth.result.access_token, auth.result.refresh_token)

          try {
            const profileRes = await this.instance.get<GetMeResponse>('/users/me')
            await setProfile(profileRes.data.result)
          } catch {
            // ignore profile fetch errors here
          }
        }

        if (url === '/users/logout') {
          this.accessToken = ''
          await clearAuthStorage()
        }

        return response
      },
      async (error: AxiosError) => {
        const { response, config } = error
        const originalRequest = config as RequestConfigWithRetry

        if (response?.status === HttpStatusCode.Unauthorized && originalRequest && !originalRequest._retry) {
          originalRequest._retry = true
          const refreshToken = await getRefreshToken()

          if (!refreshToken) {
            this.accessToken = ''
            await clearAuthStorage()
            return Promise.reject(error)
          }

          try {
            const refreshRes = await axios.post<AuthResponse>(`${baseURL}/users/refresh-token`, {
              refresh_token: refreshToken
            })

            this.accessToken = refreshRes.data.result.access_token
            await setTokens(refreshRes.data.result.access_token, refreshRes.data.result.refresh_token)

            const headers = AxiosHeaders.from(originalRequest.headers)
            headers.set('Authorization', `Bearer ${this.accessToken}`)
            originalRequest.headers = headers
            return this.instance(originalRequest)
          } catch (refreshError) {
            this.accessToken = ''
            await clearAuthStorage()
            return Promise.reject(refreshError)
          }
        }

        return Promise.reject(error)
      }
    )
  }
}

const http = new Http().instance
export default http
