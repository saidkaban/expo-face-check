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
  // Multiple faces, none dominant
}
```

## API

### `checkFace(imageUri: string): Promise<FaceCheckResult>`

Analyzes a local image for faces.

**Parameters:**
- `imageUri` — Local file URI of the image (e.g., from `expo-image-picker`)

**Returns:** `FaceCheckResult`

```typescript
type FaceCheckStatus = 'READY' | 'NO_FACE' | 'MULTIPLE_FACES';

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
```

### Status values

| Status | Meaning |
|--------|---------|
| `READY` | Exactly one dominant face detected |
| `NO_FACE` | No faces found in the image |
| `MULTIPLE_FACES` | Multiple faces found, none clearly dominant |

A face is considered "dominant" when it's either the only face, or at least 2x larger than the next biggest face. Faces smaller than 1.5% of the image area are ignored.

## License

MIT
