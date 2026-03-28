---
sidebar_position: 6
title: Examples
---

# Examples

Complete working examples using expo-face-check.

## Profile Photo Validator

A complete component that lets users pick a photo and validates it has exactly one face.

```typescript
import React, { useState } from 'react';
import { View, Text, Image, TouchableOpacity, StyleSheet, ActivityIndicator } from 'react-native';
import * as ImagePicker from 'expo-image-picker';
import { checkFace, type FaceCheckResult } from 'expo-face-check';

export default function ProfilePhotoValidator() {
  const [imageUri, setImageUri] = useState<string | null>(null);
  const [result, setResult] = useState<FaceCheckResult | null>(null);
  const [loading, setLoading] = useState(false);

  const pickAndCheck = async () => {
    const picker = await ImagePicker.launchImageLibraryAsync({
      mediaTypes: ['images'],
      quality: 1,
    });

    if (picker.canceled) return;

    const uri = picker.assets[0].uri;
    setImageUri(uri);
    setLoading(true);

    try {
      const faceResult = await checkFace(uri);
      setResult(faceResult);
    } catch (error) {
      console.error('Face check failed:', error);
    } finally {
      setLoading(false);
    }
  };

  const statusColor = {
    READY: '#10b981',
    NO_FACE: '#ef4444',
    MULTIPLE_FACES: '#f59e0b',
  };

  return (
    <View style={styles.container}>
      <TouchableOpacity style={styles.button} onPress={pickAndCheck}>
        <Text style={styles.buttonText}>Pick Photo</Text>
      </TouchableOpacity>

      {imageUri && (
        <Image source={{ uri: imageUri }} style={styles.image} />
      )}

      {loading && <ActivityIndicator size="large" color="#0d9488" />}

      {result && !loading && (
        <View style={[styles.result, { borderColor: statusColor[result.status] }]}>
          <Text style={[styles.status, { color: statusColor[result.status] }]}>
            {result.status}
          </Text>
          <Text style={styles.count}>Faces detected: {result.faceCount}</Text>
          {result.dominantFaceBounds && (
            <Text style={styles.bounds}>
              Face bounds: {JSON.stringify(result.dominantFaceBounds, null, 2)}
            </Text>
          )}
        </View>
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, padding: 20, alignItems: 'center', gap: 16 },
  button: {
    backgroundColor: '#0d9488',
    paddingHorizontal: 24,
    paddingVertical: 12,
    borderRadius: 8,
  },
  buttonText: { color: '#fff', fontSize: 16, fontWeight: '600' },
  image: { width: 300, height: 300, borderRadius: 12 },
  result: { padding: 16, borderRadius: 12, borderWidth: 2, width: '100%' },
  status: { fontSize: 20, fontWeight: '700' },
  count: { fontSize: 14, color: '#6b7280', marginTop: 4 },
  bounds: { fontSize: 12, color: '#9ca3af', marginTop: 8, fontFamily: 'monospace' },
});
```

## Camera Capture + Face Check

Capture a photo with the camera and immediately validate it.

```typescript
import { CameraView, useCameraPermissions } from 'expo-camera';
import { checkFace } from 'expo-face-check';
import { useRef } from 'react';

export function CameraFaceCheck() {
  const cameraRef = useRef<CameraView>(null);
  const [permission, requestPermission] = useCameraPermissions();

  const capture = async () => {
    const photo = await cameraRef.current?.takePictureAsync();
    if (!photo) return;

    const result = await checkFace(photo.uri);

    if (result.status === 'READY') {
      // Valid selfie — proceed with upload
      console.log('Good selfie!', result.dominantFaceBounds);
    } else {
      // Prompt user to retake
      alert('Please take a clear photo of your face');
    }
  };

  // ... render camera + capture button
}
```

## Batch Face Checking

Validate multiple images at once (e.g., for a photo gallery).

```typescript
import { checkFace, type FaceCheckResult } from 'expo-face-check';

async function checkMultipleImages(uris: string[]) {
  const results = await Promise.all(
    uris.map(async (uri) => {
      try {
        const result = await checkFace(uri);
        return { uri, result, error: null };
      } catch (error) {
        return { uri, result: null, error: error.message };
      }
    })
  );

  const validPhotos = results.filter((r) => r.result?.status === 'READY');
  const invalidPhotos = results.filter((r) => r.result?.status !== 'READY');

  console.log(`${validPhotos.length} valid, ${invalidPhotos.length} invalid`);
  return results;
}
```

## Example App

A full example app is included in the repository at [`example/`](https://github.com/saidkaban/expo-face-check/tree/main/example). To run it:

```bash
git clone https://github.com/saidkaban/expo-face-check.git
cd expo-face-check/example
npm install
npx expo prebuild --clean
npx expo run:ios    # or npx expo run:android
```
