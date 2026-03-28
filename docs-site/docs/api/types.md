---
sidebar_position: 2
title: Types
---

# Types

All types exported by expo-face-check.

```typescript
import type { FaceCheckResult, FaceCheckStatus, FaceBounds } from 'expo-face-check';
```

## FaceCheckResult

The result object returned by [`checkFace()`](/api/check-face).

```typescript
interface FaceCheckResult {
  status: FaceCheckStatus;
  faceCount: number;
  dominantFaceBounds?: FaceBounds;
}
```

| Property | Type | Description |
|----------|------|-------------|
| `status` | [`FaceCheckStatus`](#facecheckstatus) | The detection result status |
| `faceCount` | `number` | Total number of detected faces (after filtering) |
| `dominantFaceBounds` | [`FaceBounds`](#facebounds) \| `undefined` | Bounding box of the dominant face. Only present when `status` is `'READY'`. |

## FaceCheckStatus

A string literal union representing the detection result.

```typescript
type FaceCheckStatus = 'READY' | 'NO_FACE' | 'MULTIPLE_FACES';
```

| Value | Description |
|-------|-------------|
| `'READY'` | Exactly one dominant face detected. `dominantFaceBounds` will be present. |
| `'NO_FACE'` | No faces found in the image (or all detected faces were too small). |
| `'MULTIPLE_FACES'` | Multiple faces found, but none is clearly dominant (largest face is less than 2× the second-largest). |

## FaceBounds

Bounding box coordinates for a detected face, in pixels.

```typescript
interface FaceBounds {
  x: number;
  y: number;
  width: number;
  height: number;
}
```

| Property | Type | Description |
|----------|------|-------------|
| `x` | `number` | X coordinate of the top-left corner (pixels) |
| `y` | `number` | Y coordinate of the top-left corner (pixels) |
| `width` | `number` | Width of the bounding box (pixels) |
| `height` | `number` | Height of the bounding box (pixels) |

:::info Coordinate System
Coordinates use a **top-left origin** system on both platforms. On iOS, the native Vision framework uses bottom-left origin coordinates — expo-face-check automatically converts these for you.
:::
