---
sidebar_position: 1
title: checkFace()
---

# checkFace()

Analyzes a local image for faces and determines if a single dominant face is present.

```typescript
function checkFace(imageUri: string): Promise<FaceCheckResult>
```

## Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| `imageUri` | `string` | Local file URI of the image to analyze. Accepts `file://` URIs, plain file paths, and `content://` URIs (Android only). |

## Returns

`Promise<FaceCheckResult>` — A promise that resolves to a [`FaceCheckResult`](/api/types#facecheckresult) object.

## Usage

```typescript
import { checkFace } from 'expo-face-check';

const result = await checkFace('file:///path/to/image.jpg');

console.log(result.status);             // 'READY' | 'NO_FACE' | 'MULTIPLE_FACES'
console.log(result.faceCount);          // number
console.log(result.dominantFaceBounds); // { x, y, width, height } or undefined
```

## Status Values

| Status | Meaning | `dominantFaceBounds` |
|--------|---------|---------------------|
| `READY` | Exactly one dominant face detected | ✅ Present |
| `NO_FACE` | No faces found in the image | ❌ `undefined` |
| `MULTIPLE_FACES` | Multiple faces found, none clearly dominant | ❌ `undefined` |

## Examples

### Profile Photo Validation

```typescript
import * as ImagePicker from 'expo-image-picker';
import { checkFace } from 'expo-face-check';

async function validateProfilePhoto() {
  const picker = await ImagePicker.launchImageLibraryAsync({
    mediaTypes: ['images'],
    quality: 1,
  });

  if (picker.canceled) return;

  const result = await checkFace(picker.assets[0].uri);

  switch (result.status) {
    case 'READY':
      // Upload the photo — it's a valid profile pic
      uploadPhoto(picker.assets[0].uri);
      break;
    case 'NO_FACE':
      showError('No face detected. Please choose a photo with your face.');
      break;
    case 'MULTIPLE_FACES':
      showError('Multiple people detected. Please use a solo photo.');
      break;
  }
}
```

### Face Cropping

```typescript
import { checkFace } from 'expo-face-check';
import * as ImageManipulator from 'expo-image-manipulator';

async function cropToFace(imageUri: string) {
  const result = await checkFace(imageUri);

  if (result.status !== 'READY' || !result.dominantFaceBounds) {
    throw new Error('No dominant face found');
  }

  const { x, y, width, height } = result.dominantFaceBounds;

  // Add some padding around the face
  const padding = Math.max(width, height) * 0.3;

  const cropped = await ImageManipulator.manipulateAsync(imageUri, [
    {
      crop: {
        originX: Math.max(0, x - padding),
        originY: Math.max(0, y - padding),
        width: width + padding * 2,
        height: height + padding * 2,
      },
    },
  ]);

  return cropped.uri;
}
```

## Error Handling

`checkFace()` will throw an error if:

- The image URI is invalid or the file doesn't exist
- The image cannot be loaded or decoded
- A native framework error occurs

```typescript
try {
  const result = await checkFace(imageUri);
  // Handle result...
} catch (error) {
  console.error('Face detection failed:', error.message);
}
```

## Platform Behavior

| Behavior | iOS | Android |
|----------|-----|---------|
| Framework | Apple Vision | Google ML Kit |
| Coordinate system | Converted from bottom-left to top-left | Top-left origin (native) |
| Supported URIs | `file://`, plain paths | `file://`, `content://`, plain paths |
| Processing | Async (`userInitiated` queue) | Async (ML Kit callbacks) |
