import React from 'react'
import { Pressable, StyleSheet, Text, View } from 'react-native'
import { Event } from '../types/event'

interface Props {
  event: Event
  onPress: () => void
}

export function EventCard({ event, onPress }: Props) {
  return (
    <Pressable onPress={onPress} style={styles.card}>
      <Text style={styles.title}>{event.title}</Text>
      <Text style={styles.meta}>{event.location_text}</Text>
      <Text style={styles.meta}>{new Date(event.start_at).toLocaleString()}</Text>
      <View style={styles.row}>
        <Text style={styles.price}>{(event.price_cents / 100).toLocaleString()} VND</Text>
        <Text style={styles.status}>{event.status}</Text>
      </View>
    </Pressable>
  )
}

const styles = StyleSheet.create({
  card: {
    borderWidth: 1,
    borderColor: '#e5e7eb',
    borderRadius: 12,
    padding: 14,
    marginBottom: 10,
    backgroundColor: '#fff'
  },
  title: {
    fontSize: 16,
    fontWeight: '700',
    marginBottom: 6
  },
  meta: {
    color: '#4b5563',
    marginBottom: 4
  },
  row: {
    marginTop: 8,
    flexDirection: 'row',
    justifyContent: 'space-between'
  },
  price: {
    fontWeight: '600'
  },
  status: {
    textTransform: 'capitalize',
    color: '#6b7280'
  }
})
