import React, { useEffect, useState } from 'react'
import { ActivityIndicator, FlatList, StyleSheet, Text, View } from 'react-native'
import usersApi from '../apis/usersApi'
import { TicketApi } from '../types/ticket'
import { useAppContext } from '../contexts/AppContext'
import { MOCK_TICKETS } from '../utils/mockData'

export function MyTicketsScreen() {
  const { isDemoMode } = useAppContext()
  const [tickets, setTickets] = useState<TicketApi[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const load = async () => {
      try {
        if (isDemoMode) {
          // Use mock data in demo mode
          setTickets(MOCK_TICKETS)
        } else {
          const response = await usersApi.getMyTickets(20, 1)
          setTickets(response.data.result.tickets)
        }
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

  return (
    <FlatList
      style={styles.list}
      data={tickets}
      keyExtractor={(item) => item.id}
      renderItem={({ item }) => (
        <View style={styles.card}>
          <Text style={styles.title}>{item.event_title}</Text>
          <Text>Status: {item.ticket_status}</Text>
          <Text>Price: {(item.price_cents / 100).toLocaleString()} VND</Text>
        </View>
      )}
      ListEmptyComponent={<Text style={styles.empty}>No tickets yet.</Text>}
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
  },
  card: {
    borderWidth: 1,
    borderColor: '#e5e7eb',
    borderRadius: 12,
    padding: 14,
    marginBottom: 10,
    backgroundColor: '#fff'
  },
  title: {
    fontWeight: '700',
    marginBottom: 6
  },
  empty: {
    paddingTop: 20,
    color: '#6b7280'
  }
})
