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
