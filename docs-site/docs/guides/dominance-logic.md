---
sidebar_position: 3
title: Dominance Logic
---

# Dominance Logic

expo-face-check doesn't just count faces — it determines whether a single face is **dominant** in the image. This page explains how that logic works.

## How It Works

The detection pipeline follows these steps:

### 1. Detect all faces

The native framework (Vision on iOS, ML Kit on Android) scans the image and returns all detected face regions.

### 2. Filter small faces

Faces smaller than **1.5% of the total image area** are discarded. This prevents background faces, faces on posters, or other incidental faces from being counted.

```
minFaceArea = imageWidth × imageHeight × 0.015
```

### 3. Sort by size

Remaining faces are sorted by bounding box area (width × height) in descending order.

### 4. Apply dominance rules

| Scenario | Result |
|----------|--------|
| 0 faces after filtering | `NO_FACE` |
| Exactly 1 face | `READY` — that face is dominant |
| 2+ faces, largest is ≥ 2× the second-largest | `READY` — largest face is dominant |
| 2+ faces, largest is < 2× the second-largest | `MULTIPLE_FACES` |

## Examples

### Single person selfie
One face detected → **`READY`**

### Person with small face in background
Two faces detected. Main subject is 30% of image area, background person is 1% (filtered out as < 1.5%) → Only 1 face remains → **`READY`**

### Person with another person partially visible
Two faces detected. Main subject is 20% of image area, other person is 5%. Since 20% ≥ 2 × 5% → **`READY`** (main subject is dominant)

### Two people equally framed
Two faces detected, both around 10% of image area. 10% < 2 × 10% → **`MULTIPLE_FACES`**

### Landscape photo with no people
Zero faces detected → **`NO_FACE`**

## Why This Approach?

Simple face counting isn't enough for profile photo validation. Consider:

- A selfie in a crowd should still be valid if the subject is clearly the main person
- A poster or TV screen in the background shouldn't invalidate the photo
- Two people posing equally for the camera should be rejected

The dominance logic handles all these cases by focusing on **relative face size** rather than just presence.
