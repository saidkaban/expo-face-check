---
sidebar_position: 1
title: Platform Support
---

# Platform Support

expo-face-check runs natively on both iOS and Android using platform-specific face detection frameworks.

## iOS

| Requirement | Value |
|-------------|-------|
| **Framework** | Apple Vision (`VNDetectFaceRectanglesRequest`) |
| **Min iOS Version** | 13.0+ |
| **Swift Version** | 5.4+ |
| **Processing** | Async on `userInitiated` dispatch queue |

The Vision framework is built into iOS — no additional dependencies or downloads are required. Face detection works offline and does not send any data to Apple's servers.

### iOS-specific behavior

- Accepts both `file://` URIs and plain file paths
- Vision returns normalized coordinates (0–1 range, origin at bottom-left) which are automatically converted to pixel coordinates (origin at top-left) for you

## Android

| Requirement | Value |
|-------------|-------|
| **Framework** | Google ML Kit Face Detection |
| **ML Kit Version** | `com.google.mlkit:face-detection:16.1.7` |
| **Min SDK Version** | API 21 (Android 5.0) |
| **Performance Mode** | `PERFORMANCE_MODE_ACCURATE` |

ML Kit's face detection model is bundled with the app — no runtime downloads needed. Detection runs entirely on-device.

### Android-specific behavior

- Accepts `file://`, `content://`, and plain file paths
- Uses `BitmapFactory` for image loading
- Bitmap is properly recycled after detection to prevent memory leaks
- Face detector resources are closed after use

## Expo Go

:::caution Not Supported
expo-face-check is a native module and **cannot run in Expo Go**. You must use a [development build](https://docs.expo.dev/develop/development-builds/introduction/).
:::

To create a development build:

```bash
npx expo prebuild --clean
npx expo run:ios    # or npx expo run:android
```

## Web

Web is not currently supported. Face detection requires native platform APIs (Vision on iOS, ML Kit on Android) that are not available in browser environments.
