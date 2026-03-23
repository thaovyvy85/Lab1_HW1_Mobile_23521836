import AsyncStorage from '@react-native-async-storage/async-storage'
import { User } from '../types/user'

const ACCESS_TOKEN = 'access_token'
const REFRESH_TOKEN = 'refresh_token'
const PROFILE = 'profile'

export const getAccessToken = async () => (await AsyncStorage.getItem(ACCESS_TOKEN)) || ''
export const getRefreshToken = async () => (await AsyncStorage.getItem(REFRESH_TOKEN)) || ''

export const setTokens = async (accessToken: string, refreshToken: string) => {
  await AsyncStorage.multiSet([
    [ACCESS_TOKEN, accessToken],
    [REFRESH_TOKEN, refreshToken]
  ])
}

export const clearAuthStorage = async () => {
  await AsyncStorage.multiRemove([ACCESS_TOKEN, REFRESH_TOKEN, PROFILE])
}

export const setProfile = async (profile: User) => {
  await AsyncStorage.setItem(PROFILE, JSON.stringify(profile))
}

export const getProfile = async (): Promise<User | null> => {
  const profile = await AsyncStorage.getItem(PROFILE)
  return profile ? (JSON.parse(profile) as User) : null
}
