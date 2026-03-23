import React, { useEffect, useState } from 'react'
import { Alert, Pressable, ScrollView, StyleSheet, Text, View } from 'react-native'
import { RouteProp, useRoute } from '@react-navigation/native'
import eventsApi from '../apis/eventsApi'
import ticketsApi from '../apis/ticketsApi'
import { RootStackParamList } from '../navigation/types'
import { Event } from '../types/event'
import { useAppContext } from '../contexts/AppContext'
import { MOCK_EVENTS } from '../utils/mockData'

type DetailRoute = RouteProp<RootStackParamList, 'EventDetail'>

export function EventDetailScreen() {
  const route = useRoute<DetailRoute>()
  const { isDemoMode } = useAppContext()
  const [event, setEvent] = useState<Event | null>(null)
  const [booking, setBooking] = useState(false)

  useEffect(() => {
    const load = async () => {
      if (isDemoMode) {
        // Use mock data in demo mode
        const mockEvent = MOCK_EVENTS.find((e) => e.id === route.params.eventId)
        setEvent(mockEvent || null)
      } else {
        const response = await eventsApi.getEventDetails(route.params.eventId)
        setEvent(response.data.result)
      }
    }

    load()
  }, [route.params.eventId, isDemoMode])

  const bookTicket = async () => {
    try {
      setBooking(true)
      await ticketsApi.bookTicket(route.params.eventId)
      Alert.alert('Success', 'Ticket booked successfully.')
    } catch {
      Alert.alert('Error', 'Could not book ticket.')
    } finally {
      setBooking(false)
    }
  }

  if (!event) {
    return (
      <View style={styles.center}>
        <Text>Loading event...</Text>
      </View>
    )
  }

  return (
    <ScrollView style={styles.container}>
      <Text style={styles.title}>{event.title}</Text>
      <Text style={styles.meta}>{event.location_text}</Text>
      <Text style={styles.meta}>Start: {new Date(event.start_at).toLocaleString()}</Text>
      <Text style={styles.meta}>End: {new Date(event.end_at).toLocaleString()}</Text>
      <Text style={styles.price}>Price: {(event.price_cents / 100).toLocaleString()} VND</Text>
      <Text style={styles.description}>{event.description || 'No description available.'}</Text>

      <Pressable onPress={bookTicket} style={styles.button} disabled={booking}>
        <Text style={styles.buttonText}>{booking ? 'Booking...' : 'Book Ticket'}</Text>
      </Pressable>
    </ScrollView>
  )
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#fff',
    padding: 16
  },
  center: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center'
  },
  title: {
    fontSize: 24,
    fontWeight: '700',
    marginBottom: 8
  },
  meta: {
    color: '#4b5563',
    marginBottom: 6
  },
  price: {
    marginTop: 8,
    fontWeight: '700'
  },
  description: {
    marginTop: 12,
    lineHeight: 22,
    color: '#111827'
  },
  button: {
    marginTop: 20,
    backgroundColor: '#111827',
    borderRadius: 10,
    alignItems: 'center',
    paddingVertical: 12
  },
  buttonText: {
    color: '#fff',
    fontWeight: '600'
  }
})
