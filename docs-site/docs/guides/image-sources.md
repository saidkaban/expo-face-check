---
sidebar_position: 2
title: Image Sources
---

# Image Sources

`checkFace()` accepts a local image URI. Here are the supported sources and formats.

## Supported URI Formats

| Format | Example | iOS | Android |
|--------|---------|-----|---------|
| `file://` URI | `file:///path/to/image.jpg` | ✅ | ✅ |
| Plain file path | `/path/to/image.jpg` | ✅ | ✅ |
| `content://` URI | `content://media/...` | ❌ | ✅ |

## Common Sources

### expo-image-picker

The most common source. Both `launchImageLibraryAsync` and `launchCameraAsync` return a `file://` URI.

```typescript
import * as ImagePicker from 'expo-image-picker';
import { checkFace } from 'expo-face-check';

const result = await ImagePicker.launchImageLibraryAsync({
  mediaTypes: ['images'],
  quality: 1,
});

if (!result.canceled) {
  const faceResult = await checkFace(result.assets[0].uri);
}
```

### expo-camera

If you capture a photo using `expo-camera`, the returned URI works directly:

```typescript
const photo = await cameraRef.current.takePictureAsync();
const faceResult = await checkFace(photo.uri);
```

### expo-file-system

Any file managed by `expo-file-system` can be checked:

```typescript
import * as FileSystem from 'expo-file-system';
import { checkFace } from 'expo-face-check';

const fileUri = FileSystem.documentDirectory + 'photo.jpg';
const faceResult = await checkFace(fileUri);
```

## Tips

- **Use full-resolution images** — Higher resolution images produce more accurate face detection results
- **Supported formats** — JPEG, PNG, and other standard image formats supported by the platform
- **Remote URLs are not supported** — Download the image to a local file first using `expo-file-system` before passing it to `checkFace()`
