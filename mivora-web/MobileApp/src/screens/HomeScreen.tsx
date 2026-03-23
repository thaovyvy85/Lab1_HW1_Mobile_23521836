import React, { useEffect, useState } from 'react'
import { ActivityIndicator, FlatList, StyleSheet, Text, View } from 'react-native'
import { NativeStackNavigationProp } from '@react-navigation/native-stack'
import { useNavigation } from '@react-navigation/native'
import eventsApi from '../apis/eventsApi'
import { EventCard } from '../components/EventCard'
import { Event } from '../types/event'
import { RootStackParamList } from '../navigation/types'
import { useAppContext } from '../contexts/AppContext'
import { MOCK_EVENTS } from '../utils/mockData'

type Nav = NativeStackNavigationProp<RootStackParamList>

export function HomeScreen() {
  const navigation = useNavigation<Nav>()
  const { isDemoMode } = useAppContext()
  const [events, setEvents] = useState<Event[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    const load = async () => {
      try {
        if (isDemoMode) {
          // Use mock data in demo mode
          setEvents(MOCK_EVENTS)
        } else {
          const response = await eventsApi.getEvents(20, 1)
          setEvents(response.data.result.events)
        }
      } catch {
        setError('Cannot load events right now.')
      } finally {
        setLoading(false)
      }
    }

    load()
  }, [isDemoMode])

  if (loading) {
    return (
      <View style={styles.center}>
        <ActivityIndicator />
      </View>
    )
  }

  if (error) {
    return (
      <View style={styles.center}>
        <Text>{error}</Text>
      </View>
    )
  }

  return (
    <FlatList
      style={styles.list}
      data={events}
      keyExtractor={(item) => item.id}
      renderItem={({ item }) => (
        <EventCard event={item} onPress={() => navigation.navigate('EventDetail', { eventId: item.id })} />
      )}
      ListEmptyComponent={<Text>No events found.</Text>}
    />
  )
}

const styles = StyleSheet.create({
  list: {
    flex: 1,
    backgroundColor: '#f9fafb',
    paddingHorizontal: 14,
    paddingTop: 12
  },
  center: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center'
  }
})
