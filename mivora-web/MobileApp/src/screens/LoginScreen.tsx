import React, { useState } from 'react'
import { Alert, Pressable, StyleSheet, Text, TextInput, View } from 'react-native'
import { useAppContext } from '../contexts/AppContext'

export function LoginScreen() {
  const { login, isDemoMode } = useAppContext()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [loading, setLoading] = useState(false)

  const onSubmit = async () => {
    if (!email || !password) {
      Alert.alert('Validation', 'Email and password are required.')
      return
    }

    try {
      setLoading(true)
      await login(email.trim(), password)
    } catch {
      Alert.alert('Login failed', 'Please check your credentials and try again.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Welcome to Mivora</Text>
      {isDemoMode && <Text style={styles.demoLabel}>🎨 DEMO MODE - Layout Preview</Text>}
      <TextInput
        autoCapitalize='none'
        keyboardType='email-address'
        placeholder='Email'
        value={email}
        onChangeText={setEmail}
        style={styles.input}
        editable={!isDemoMode}
      />
      <TextInput
        placeholder='Password'
        secureTextEntry
        value={password}
        onChangeText={setPassword}
        style={styles.input}
        editable={!isDemoMode}
      />
      <Pressable onPress={onSubmit} style={styles.button} disabled={loading || isDemoMode}>
        <Text style={styles.buttonText}>
          {isDemoMode ? 'Auto-logged in (Demo)' : loading ? 'Signing in...' : 'Sign In'}
        </Text>
      </Pressable>
    </View>
  )
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    padding: 20,
    backgroundColor: '#fff'
  },
  title: {
    fontSize: 24,
    fontWeight: '700',
    marginBottom: 20
  },
  demoLabel: {
    textAlign: 'center',
    color: '#f59e0b',
    marginBottom: 12,
    fontSize: 12,
    fontWeight: '600'
  },
  input: {
    borderWidth: 1,
    borderColor: '#d1d5db',
    borderRadius: 10,
    paddingHorizontal: 12,
    paddingVertical: 10,
    marginBottom: 12
  },
  button: {
    backgroundColor: '#111827',
    paddingVertical: 12,
    borderRadius: 10,
    alignItems: 'center'
  },
  buttonText: {
    color: '#fff',
    fontWeight: '600'
  }
})
