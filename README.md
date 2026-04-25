# expo-face-check

On-device face detection for Expo — detects if an image has exactly one dominant face. Runs entirely on-device with no network calls.

- **iOS**: Apple Vision framework
- **Android**: Google ML Kit Face Detection

## Installation

```bash
npx expo install expo-face-check
```

## Usage

```typescript
import { checkFace } from 'expo-face-check';

const result = await checkFace(imageUri);

if (result.status === 'READY') {
  // Single dominant face detected
  console.log(result.dominantFaceBounds); // { x, y, width, height }
} else if (result.status === 'NO_FACE') {
  // No face found
} else if (result.status === 'MULTIPLE_FACES') {
  // More than one dominant face detected
} else if (result.status === 'LOW_QUALITY') {
  // Image resolution below the minimum pixel count
}
```

## API

### `checkFace(imageUri: string, options?: CheckFaceOptions): Promise<FaceCheckResult>`

Analyzes a local image for faces.

**Parameters:**
- `imageUri` — Local file URI of the image (e.g., from `expo-image-picker`)
- `options` — Optional thresholds (see `CheckFaceOptions` below)

**Returns:** `FaceCheckResult`

```typescript
type FaceCheckStatus = 'READY' | 'NO_FACE' | 'MULTIPLE_FACES' | 'LOW_QUALITY';

interface FaceBounds {
  x: number;
  y: number;
  width: number;
  height: number;
}

interface FaceCheckResult {
  status: FaceCheckStatus;
  faceCount: number;
  dominantFaceBounds?: FaceBounds;
}

interface CheckFaceOptions {
  /** Minimum total pixels (width * height) for the image to be considered. Defaults to 500_000. */
  minPixelSize?: number;
  /** Faces with area smaller than `areaThreshold * largestFaceArea` are ignored when counting dominant faces. Range 0–1. Defaults to 0.2. */
  areaThreshold?: number;
}
```

### Options

| Option | Default | Meaning |
|--------|---------|---------|
| `minPixelSize` | `500_000` | Minimum total pixels (width × height). Images below this are returned as `LOW_QUALITY`. Useful to lower for compressed selfies from chat apps. |
| `areaThreshold` | `0.2` | A face counts as "dominant" when its bounding-box area is greater than `areaThreshold × largestFaceArea`. Must be between 0 and 1. |

Invalid options (negative `minPixelSize`, `areaThreshold` outside `[0, 1]`, `NaN`/infinite values) reject the promise with code `ERR_INVALID_OPTIONS`.

#### Example: lower the pixel-count floor

```typescript
const result = await checkFace(uri, { minPixelSize: 100_000 });
```

### Status values

| Status | Meaning |
|--------|---------|
| `READY` | Exactly one dominant face detected |
| `NO_FACE` | No faces found in the image |
| `MULTIPLE_FACES` | Multiple dominant faces found |
| `LOW_QUALITY` | Image resolution below `minPixelSize` |

By default, images with fewer than 500,000 pixels (e.g. below ~707×707) are rejected as `LOW_QUALITY`. Otherwise, a face is counted as "dominant" when its bounding-box area is greater than 20% of the largest detected face's area. `READY` requires exactly one dominant face; two or more yield `MULTIPLE_FACES`. EXIF orientation is applied before detection.

## License

MIT
