---
sidebar_position: 5
title: Troubleshooting
---

# Troubleshooting

Common issues and solutions when using expo-face-check.

## "expo-face-check" is not available in Expo Go

expo-face-check is a native module and **cannot run in Expo Go**. You must use a [development build](https://docs.expo.dev/develop/development-builds/introduction/).

```bash
npx expo prebuild --clean
npx expo run:ios    # or npx expo run:android
```

## Face detection returns NO_FACE when a face is present

This can happen if:

- **The face is too small** — Faces smaller than 1.5% of the image area are filtered out. Try using a higher resolution image or a closer crop.
- **The face is obscured** — Heavy occlusion (sunglasses, masks, extreme angles) may prevent detection.
- **Poor image quality** — Very dark, blurry, or low-contrast images may not produce reliable detections.

## Face detection returns MULTIPLE_FACES for a solo photo

This can happen when:

- **Background faces** — People in the background, faces on screens/posters, or mannequins may be detected as additional faces. If the background face is larger than 1.5% of image area and more than half the size of the main face, it will trigger `MULTIPLE_FACES`.
- **Reflections** — A mirror or reflective surface may cause the same face to be detected twice.

## Android: Image loading fails

Ensure your URI format is correct:

```typescript
// ✅ Correct
await checkFace('file:///data/user/0/com.app/cache/image.jpg');
await checkFace('content://media/external/images/media/123');

// ❌ Incorrect — remote URLs are not supported
await checkFace('https://example.com/photo.jpg');
```

## iOS: Build errors with Vision framework

The Vision framework is included in iOS 13.0+ and requires no additional configuration. If you see build errors:

1. Ensure your iOS deployment target is 13.0 or higher
2. Run `npx expo prebuild --clean` to regenerate native projects
3. Run `cd ios && pod install` to update CocoaPods dependencies

## Performance

Face detection is fast on modern devices:

- **iOS**: Typically < 100ms for a standard photo
- **Android**: Typically < 200ms using `PERFORMANCE_MODE_ACCURATE`

If detection feels slow, consider:

- Reducing image resolution before detection (face detection doesn't need 4K images)
- Running detection off the main thread (the native implementation already does this, but ensure your JS code isn't blocking the UI)

## Still stuck?

[Open an issue](https://github.com/saidkaban/expo-face-check/issues) on GitHub with:

- Your platform (iOS/Android) and version
- expo-face-check version
- A minimal reproduction
- The error message (if any)
