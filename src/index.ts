import ExpoFaceCheckModule from './ExpoFaceCheckModule';
import type {
  FaceCheckResult,
  FaceCheckStatus,
  FaceBounds,
  CheckFaceOptions,
} from './ExpoFaceCheck.types';

export type { FaceCheckResult, FaceCheckStatus, FaceBounds, CheckFaceOptions };

/**
 * Detects faces in a local image and determines if there is exactly one dominant face.
 * Runs entirely on-device with no network calls.
 *
 * @param imageUri - Local file URI of the image to analyze
 * @param options - Optional thresholds for image size and dominant-face filtering
 * @returns Face detection result with status, count, and optional dominant face bounds
 */
export async function checkFace(
  imageUri: string,
  options?: CheckFaceOptions
): Promise<FaceCheckResult> {
  return ExpoFaceCheckModule.checkFace(imageUri, options ?? {});
}
