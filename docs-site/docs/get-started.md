---
sidebar_position: 2
title: Get Started
---

# Get Started

Install expo-face-check and run your first on-device face detection in minutes.

## Prerequisites

- An existing [Expo](https://expo.dev) project (SDK 51+)
- iOS 13.0+ or Android API 21+
- [Development build](https://docs.expo.dev/develop/development-builds/introduction/) (not Expo Go — native modules require a dev build)

## Installation

```bash
npx expo install expo-face-check
```

Then rebuild your development client:

```bash
npx expo prebuild --clean
npx expo run:ios    # or npx expo run:android
```

## Basic Usage

### Simple Face Check

```typescript
import { checkFace } from 'expo-face-check';

const result = await checkFace(imageUri);
console.log(result.status);      // 'READY' | 'NO_FACE' | 'MULTIPLE_FACES'
console.log(result.faceCount);   // number of faces detected
```

### With Image Picker

A common pattern is combining expo-face-check with `expo-image-picker` for profile photo validation:

```typescript
import * as ImagePicker from 'expo-image-picker';
import { checkFace } from 'expo-face-check';

async function pickAndValidatePhoto() {
  // 1. Pick an image
  const pickerResult = await ImagePicker.launchImageLibraryAsync({
    mediaTypes: ['images'],
    quality: 1,
  });

  if (pickerResult.canceled) return;

  const imageUri = pickerResult.assets[0].uri;

  // 2. Check for a face
  const faceResult = await checkFace(imageUri);

  switch (faceResult.status) {
    case 'READY':
      console.log('Valid profile photo!');
      console.log('Face at:', faceResult.dominantFaceBounds);
      break;
    case 'NO_FACE':
      alert('No face detected. Please choose a different photo.');
      break;
    case 'MULTIPLE_FACES':
      alert('Multiple people detected. Please use a solo photo.');
      break;
  }
}
```

### Using Face Bounds

When a dominant face is detected, you get its bounding box in pixel coordinates:

```typescript
const result = await checkFace(imageUri);

if (result.status === 'READY' && result.dominantFaceBounds) {
  const { x, y, width, height } = result.dominantFaceBounds;

  // Use for cropping, overlay positioning, etc.
  console.log(`Face at (${x}, ${y}), size ${width}x${height}`);
}
```

## Next Steps

- **[Dominance Logic](/guides/dominance-logic)** — How "dominant face" detection works
- **[Image Sources](/guides/image-sources)** — Supported image URI formats
- **[API Reference](/api/check-face)** — Full API documentation
- **[Examples](/examples)** — Complete working app examples
