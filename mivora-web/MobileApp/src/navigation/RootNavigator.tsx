import React from 'react'
import { ActivityIndicator, View } from 'react-native'
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs'
import { createNativeStackNavigator } from '@react-navigation/native-stack'
import { useAppContext } from '../contexts/AppContext'
import { EventDetailScreen } from '../screens/EventDetailScreen'
import { HomeScreen } from '../screens/HomeScreen'
import { LoginScreen } from '../screens/LoginScreen'
import { MyTicketsScreen } from '../screens/MyTicketsScreen'
import { ProfileScreen } from '../screens/ProfileScreen'
import { MainTabParamList, RootStackParamList } from './types'

const Stack = createNativeStackNavigator<RootStackParamList>()
const Tab = createBottomTabNavigator<MainTabParamList>()

function MainTabs() {
  return (
    <Tab.Navigator>
      <Tab.Screen name='Home' component={HomeScreen} />
      <Tab.Screen name='MyTickets' component={MyTicketsScreen} options={{ title: 'My Tickets' }} />
      <Tab.Screen name='Profile' component={ProfileScreen} />
    </Tab.Navigator>
  )
}

export function RootNavigator() {
  const { isAuthenticated, isInitializing } = useAppContext()

  if (isInitializing) {
    return (
      <View style={{ flex: 1, alignItems: 'center', justifyContent: 'center' }}>
        <ActivityIndicator />
      </View>
    )
  }

  return (
    <Stack.Navigator>
      {isAuthenticated ? (
        <>
          <Stack.Screen name='MainTabs' component={MainTabs} options={{ headerShown: false }} />
          <Stack.Screen name='EventDetail' component={EventDetailScreen} options={{ title: 'Event Detail' }} />
        </>
      ) : (
        <Stack.Screen name='Login' component={LoginScreen} options={{ title: 'Mivora Login' }} />
      )}
    </Stack.Navigator>
  )
}
