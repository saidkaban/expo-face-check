import ExpoModulesCore
import Vision
import UIKit

public class ExpoFaceCheckModule: Module {
  public func definition() -> ModuleDefinition {
    Name("ExpoFaceCheck")

    AsyncFunction("checkFace") { (imageUri: String, promise: Promise) in
      guard let image = self.loadImage(from: imageUri) else {
        promise.reject("ERR_LOAD_IMAGE", "Failed to load image from URI: \(imageUri)")
        return
      }

      guard let cgImage = image.cgImage else {
        promise.reject("ERR_LOAD_IMAGE", "Failed to get CGImage from loaded image")
        return
      }

      let imageWidth = CGFloat(cgImage.width)
      let imageHeight = CGFloat(cgImage.height)
      let imageArea = imageWidth * imageHeight
      let minFaceArea = imageArea * 0.015 // 1.5% threshold

      let request = VNDetectFaceRectanglesRequest { request, error in
        if let error = error {
          promise.reject("ERR_FACE_DETECTION", "Face detection failed: \(error.localizedDescription)")
          return
        }

        guard let results = request.results as? [VNFaceObservation] else {
          promise.resolve([
            "status": "NO_FACE",
            "faceCount": 0
          ])
          return
        }

        // Convert Vision normalized coordinates to pixel coordinates and filter small faces
        var significantFaces: [(bounds: CGRect, area: CGFloat)] = []

        for face in results {
          let bbox = face.boundingBox
          // Vision coordinates: origin is bottom-left, normalized 0-1
          let pixelX = bbox.origin.x * imageWidth
          let pixelY = (1.0 - bbox.origin.y - bbox.height) * imageHeight
          let pixelWidth = bbox.width * imageWidth
          let pixelHeight = bbox.height * imageHeight
          let faceArea = pixelWidth * pixelHeight

          if faceArea >= minFaceArea {
            let pixelBounds = CGRect(x: pixelX, y: pixelY, width: pixelWidth, height: pixelHeight)
            significantFaces.append((bounds: pixelBounds, area: faceArea))
          }
        }

        // Sort by area descending
        significantFaces.sort { $0.area > $1.area }

        let faceCount = significantFaces.count

        if faceCount == 0 {
          promise.resolve([
            "status": "NO_FACE",
            "faceCount": 0
          ])
        } else if faceCount == 1 {
          let dominant = significantFaces[0]
          promise.resolve([
            "status": "READY",
            "faceCount": 1,
            "dominantFaceBounds": [
              "x": dominant.bounds.origin.x,
              "y": dominant.bounds.origin.y,
              "width": dominant.bounds.width,
              "height": dominant.bounds.height
            ]
          ])
        } else {
          // Check dominance: largest >= 2x second largest
          let largestArea = significantFaces[0].area
          let secondArea = significantFaces[1].area

          if largestArea >= 2.0 * secondArea {
            let dominant = significantFaces[0]
            promise.resolve([
              "status": "READY",
              "faceCount": faceCount,
              "dominantFaceBounds": [
                "x": dominant.bounds.origin.x,
                "y": dominant.bounds.origin.y,
                "width": dominant.bounds.width,
                "height": dominant.bounds.height
              ]
            ])
          } else {
            promise.resolve([
              "status": "MULTIPLE_FACES",
              "faceCount": faceCount
            ])
          }
        }
      }

      let handler = VNImageRequestHandler(cgImage: cgImage, options: [:])
      DispatchQueue.global(qos: .userInitiated).async {
        do {
          try handler.perform([request])
        } catch {
          promise.reject("ERR_FACE_DETECTION", "Failed to perform face detection: \(error.localizedDescription)")
        }
      }
    }
  }

  private func loadImage(from uri: String) -> UIImage? {
    // Handle file:// URIs and plain paths
    let path: String
    if uri.hasPrefix("file://") {
      guard let url = URL(string: uri) else { return nil }
      path = url.path
    } else if uri.hasPrefix("/") {
      path = uri
    } else {
      // Try as a URL
      guard let url = URL(string: uri), let data = try? Data(contentsOf: url) else { return nil }
      return UIImage(data: data)
    }

    return UIImage(contentsOfFile: path)
  }
}
