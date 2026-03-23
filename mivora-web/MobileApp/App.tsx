import React from 'react'
import { NavigationContainer } from '@react-navigation/native'
import { StatusBar } from 'expo-status-bar'
import { SafeAreaProvider } from 'react-native-safe-area-context'
import { Platform, StyleSheet, View } from 'react-native'
import { AppProvider } from './src/contexts/AppContext'
import { RootNavigator } from './src/navigation/RootNavigator'

export default function App() {
  return (
    <View style={styles.shell}>
      <View style={styles.phone}>
        <SafeAreaProvider>
          <AppProvider>
            <NavigationContainer>
              {/* Android typically uses 'auto' or 'light'/inverted bars depending on the theme */}
              <StatusBar style="auto" backgroundColor="transparent" translucent />
              <RootNavigator />
            </NavigationContainer>
          </AppProvider>
        </SafeAreaProvider>
      </View>
    </View>
  )
}

const styles = StyleSheet.create({
  shell: {
    flex: 1,
    backgroundColor: '#121212', // Darker background often used in Android previews
    ...(Platform.OS === 'web' && {
      alignItems: 'center' as const,
      justifyContent: 'center' as const,
      padding: 20,
    }),
  },
  phone: {
    flex: 1,
    backgroundColor: '#fff',
    overflow: 'hidden' as const,
    ...(Platform.OS === 'web' && {
      // Modern Android flagships (like Pixel 8) are slightly wider/shorter than iPhones
      width: 412, 
      maxHeight: 892,
      // Android corners are usually less aggressive than iPhone's deep curves
      borderRadius: 28, 
      borderWidth: 8,
      borderColor: '#2d2d2d', // Mocking a slim bezel
      // Elevation-style shadow for Android
      boxShadow: '0 10px 30px rgba(0,0,0,0.5)', 
    }),
  },
})