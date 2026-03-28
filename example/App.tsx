import { useState } from 'react';
import { StyleSheet, Text, View, Image, Pressable, ActivityIndicator } from 'react-native';
import * as ImagePicker from 'expo-image-picker';
import { checkFace, type FaceCheckResult } from 'expo-face-check';

const STATUS_COLORS: Record<string, string> = {
  READY: '#22c55e',
  NO_FACE: '#ef4444',
  MULTIPLE_FACES: '#f59e0b',
};

export default function App() {
  const [imageUri, setImageUri] = useState<string | null>(null);
  const [result, setResult] = useState<FaceCheckResult | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const pickAndCheck = async () => {
    try {
      const picked = await ImagePicker.launchImageLibraryAsync({
        mediaTypes: ['images'],
        quality: 1,
      });

      if (picked.canceled || !picked.assets[0]) return;

      const uri = picked.assets[0].uri;
      setImageUri(uri);
      setResult(null);
      setError(null);
      setLoading(true);

      const faceResult = await checkFace(uri);
      setResult(faceResult);
    } catch (e: any) {
      setError(e.message ?? 'Unknown error');
    } finally {
      setLoading(false);
    }
  };

  return (
    <View style={styles.container}>
      <Text style={styles.title}>expo-face-check</Text>

      <Pressable style={styles.button} onPress={pickAndCheck}>
        <Text style={styles.buttonText}>Pick an Image</Text>
      </Pressable>

      {imageUri && (
        <Image source={{ uri: imageUri }} style={styles.preview} resizeMode="contain" />
      )}

      {loading && <ActivityIndicator size="large" style={styles.loader} />}

      {error && <Text style={styles.error}>{error}</Text>}

      {result && (
        <View style={styles.resultCard}>
          <Text style={[styles.status, { color: STATUS_COLORS[result.status] ?? '#888' }]}>
            {result.status}
          </Text>
          <Text style={styles.detail}>Faces detected: {result.faceCount}</Text>
          {result.dominantFaceBounds && (
            <Text style={styles.detail}>
              Bounds: ({Math.round(result.dominantFaceBounds.x)},{' '}
              {Math.round(result.dominantFaceBounds.y)}) —{' '}
              {Math.round(result.dominantFaceBounds.width)}x
              {Math.round(result.dominantFaceBounds.height)}
            </Text>
          )}
        </View>
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#0f172a',
    alignItems: 'center',
    justifyContent: 'center',
    padding: 24,
  },
  title: {
    fontSize: 28,
    fontWeight: '700',
    color: '#f8fafc',
    marginBottom: 24,
  },
  button: {
    backgroundColor: '#3b82f6',
    paddingHorizontal: 24,
    paddingVertical: 14,
    borderRadius: 12,
    marginBottom: 24,
  },
  buttonText: {
    color: '#fff',
    fontSize: 16,
    fontWeight: '600',
  },
  preview: {
    width: 260,
    height: 260,
    borderRadius: 12,
    marginBottom: 20,
    backgroundColor: '#1e293b',
  },
  loader: {
    marginVertical: 16,
  },
  error: {
    color: '#ef4444',
    fontSize: 14,
    marginTop: 12,
  },
  resultCard: {
    backgroundColor: '#1e293b',
    borderRadius: 12,
    padding: 20,
    width: '100%',
    alignItems: 'center',
  },
  status: {
    fontSize: 22,
    fontWeight: '700',
    marginBottom: 8,
  },
  detail: {
    color: '#94a3b8',
    fontSize: 14,
    marginTop: 4,
  },
});
