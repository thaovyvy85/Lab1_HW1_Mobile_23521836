import React from 'react'
import { Pressable, StyleSheet, Text, View } from 'react-native'
import { useAppContext } from '../contexts/AppContext'

export function ProfileScreen() {
  const { profile, logout } = useAppContext()

  return (
    <View style={styles.container}>
      <Text style={styles.name}>{profile?.name || 'Unknown user'}</Text>
      <Text style={styles.email}>{profile?.email || '-'}</Text>
      <Text style={styles.role}>Roles: {profile?.role?.join(', ') || '-'}</Text>

      <Pressable style={styles.button} onPress={logout}>
        <Text style={styles.buttonText}>Logout</Text>
      </Pressable>
    </View>
  )
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#fff',
    padding: 18
  },
  name: {
    fontSize: 22,
    fontWeight: '700',
    marginBottom: 6
  },
  email: {
    color: '#4b5563',
    marginBottom: 4
  },
  role: {
    color: '#4b5563'
  },
  button: {
    marginTop: 24,
    backgroundColor: '#ef4444',
    borderRadius: 10,
    paddingVertical: 12,
    alignItems: 'center'
  },
  buttonText: {
    color: '#fff',
    fontWeight: '600'
  }
})
