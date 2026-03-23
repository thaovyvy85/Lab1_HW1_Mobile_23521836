import React, { createContext, useContext, useEffect, useMemo, useState } from 'react'
import usersApi from '../apis/usersApi'
import { User } from '../types/user'
import { clearAuthStorage, getAccessToken, getProfile, getRefreshToken, setProfile } from '../utils/storage'
import { MOCK_USER } from '../utils/mockData'

// ===== TOGGLE DEMO MODE HERE =====
const DEMO_MODE = true // Set to true to skip login; false for real backend
// =================================

interface AppContextValue {
  isInitializing: boolean
  isAuthenticated: boolean
  profile: User | null
  isDemoMode: boolean
  login: (email: string, password: string) => Promise<void>
  logout: () => Promise<void>
  refreshProfile: () => Promise<void>
}

const AppContext = createContext<AppContextValue | undefined>(undefined)

export const AppProvider = ({ children }: { children: React.ReactNode }) => {
  const [isInitializing, setIsInitializing] = useState(true)
  const [isAuthenticated, setIsAuthenticated] = useState(false)
  const [profile, setProfileState] = useState<User | null>(null)

  useEffect(() => {
    const bootstrap = async () => {
      if (DEMO_MODE) {
        // In demo mode, auto-login with mock user
        setIsAuthenticated(true)
        setProfileState(MOCK_USER)
      } else {
        // Real mode: check for stored token
        const accessToken = await getAccessToken()
        const cachedProfile = await getProfile()

        setIsAuthenticated(Boolean(accessToken))
        setProfileState(cachedProfile)
      }
      setIsInitializing(false)
    }

    bootstrap()
  }, [])

  const refreshProfile = async () => {
    const response = await usersApi.getMe()
    setProfileState(response.data.result)
    await setProfile(response.data.result)
  }

  const login = async (email: string, password: string) => {
    if (DEMO_MODE) {
      setIsAuthenticated(true)
      setProfileState(MOCK_USER)
      return
    }
    await usersApi.loginAccount({ email, password })
    await refreshProfile()
    setIsAuthenticated(true)
  }

  const logout = async () => {
    if (DEMO_MODE) {
      setIsAuthenticated(false)
      setProfileState(null)
      return
    }
    const refreshToken = await getRefreshToken()
    try {
      if (refreshToken) {
        await usersApi.logout(refreshToken)
      }
    } finally {
      await clearAuthStorage()
      setIsAuthenticated(false)
      setProfileState(null)
    }
  }

  const value = useMemo(
    () => ({
      isInitializing,
      isAuthenticated,
      profile,
      isDemoMode: DEMO_MODE,
      login,
      logout,
      refreshProfile
    }),
    [isInitializing, isAuthenticated, profile]
  )

  return <AppContext.Provider value={value}>{children}</AppContext.Provider>
}

export const useAppContext = () => {
  const context = useContext(AppContext)
  if (!context) {
    throw new Error('useAppContext must be used within AppProvider')
  }
  return context
}
