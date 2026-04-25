export type FaceCheckStatus = 'READY' | 'NO_FACE' | 'MULTIPLE_FACES' | 'LOW_QUALITY';

export interface FaceBounds {
  x: number;
  y: number;
  width: number;
  height: number;
}

export interface FaceCheckResult {
  status: FaceCheckStatus;
  faceCount: number;
  dominantFaceBounds?: FaceBounds;
}

export interface CheckFaceOptions {
  /** Minimum total pixels (width * height) for the image to be considered. Defaults to 500_000. */
  minPixelSize?: number;
  /** Faces with area smaller than `areaThreshold * largestFaceArea` are ignored when counting dominant faces. Range 0–1. Defaults to 0.2. */
  areaThreshold?: number;
}
