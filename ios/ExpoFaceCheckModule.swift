import ExpoModulesCore
import Vision
import UIKit

private let DEFAULT_MIN_PIXEL_SIZE: CGFloat = 500_000
private let DEFAULT_AREA_THRESHOLD: CGFloat = 0.2

public class ExpoFaceCheckModule: Module {
  public func definition() -> ModuleDefinition {
    Name("ExpoFaceCheck")

    AsyncFunction("checkFace") { (imageUri: String, options: [String: Any]?, promise: Promise) in
      let minPixelSize: CGFloat
      let areaThreshold: CGFloat

      if let raw = options?["minPixelSize"] {
        guard let value = (raw as? NSNumber)?.doubleValue, value.isFinite, value >= 0 else {
          promise.reject("ERR_INVALID_OPTIONS", "minPixelSize must be a non-negative finite number")
          return
        }
        minPixelSize = CGFloat(value)
      } else {
        minPixelSize = DEFAULT_MIN_PIXEL_SIZE
      }

      if let raw = options?["areaThreshold"] {
        guard let value = (raw as? NSNumber)?.doubleValue, value.isFinite, value >= 0, value <= 1 else {
          promise.reject("ERR_INVALID_OPTIONS", "areaThreshold must be a number between 0 and 1")
          return
        }
        areaThreshold = CGFloat(value)
      } else {
        areaThreshold = DEFAULT_AREA_THRESHOLD
      }

      guard let rawImage = self.loadImage(from: imageUri) else {
        promise.reject("ERR_LOAD_IMAGE", "Failed to load image from URI: \(imageUri)")
        return
      }

      guard let image = self.normalizeOrientation(rawImage),
            let cgImage = image.cgImage else {
        promise.reject("ERR_LOAD_IMAGE", "Failed to decode image: \(imageUri)")
        return
      }

      let imageWidth = CGFloat(cgImage.width)
      let imageHeight = CGFloat(cgImage.height)

      if imageWidth * imageHeight < minPixelSize {
        promise.resolve([
          "status": "LOW_QUALITY",
          "faceCount": 0
        ])
        return
      }

      let request = VNDetectFaceRectanglesRequest { request, error in
        if let error = error {
          promise.reject("ERR_FACE_DETECTION", "Face detection failed: \(error.localizedDescription)")
          return
        }

        let results = (request.results as? [VNFaceObservation]) ?? []

        var detectedFaces: [(bounds: CGRect, area: CGFloat)] = []
        for face in results {
          let bbox = face.boundingBox
          let pixelX = bbox.origin.x * imageWidth
          let pixelY = (1.0 - bbox.origin.y - bbox.height) * imageHeight
          let pixelWidth = bbox.width * imageWidth
          let pixelHeight = bbox.height * imageHeight
          let area = abs(pixelWidth) * abs(pixelHeight)
          let pixelBounds = CGRect(x: pixelX, y: pixelY, width: pixelWidth, height: pixelHeight)
          detectedFaces.append((bounds: pixelBounds, area: area))
        }

        if detectedFaces.isEmpty {
          promise.resolve([
            "status": "NO_FACE",
            "faceCount": 0
          ])
          return
        }

        detectedFaces.sort { $0.area > $1.area }
        let maxArea = detectedFaces[0].area
        let dominantCount = detectedFaces.filter { $0.area / maxArea > areaThreshold }.count

        if dominantCount == 0 {
          promise.resolve([
            "status": "NO_FACE",
            "faceCount": 0
          ])
        } else if dominantCount == 1 {
          let dominant = detectedFaces[0]
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
          promise.resolve([
            "status": "MULTIPLE_FACES",
            "faceCount": dominantCount
          ])
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
    let path: String
    if uri.hasPrefix("file://") {
      guard let url = URL(string: uri) else { return nil }
      path = url.path
    } else if uri.hasPrefix("/") {
      path = uri
    } else {
      guard let url = URL(string: uri), let data = try? Data(contentsOf: url) else { return nil }
      return UIImage(data: data)
    }
    return UIImage(contentsOfFile: path)
  }

  private func normalizeOrientation(_ image: UIImage) -> UIImage? {
    if image.imageOrientation == .up { return image }
    let format = UIGraphicsImageRendererFormat.default()
    format.scale = image.scale
    let renderer = UIGraphicsImageRenderer(size: image.size, format: format)
    return renderer.image { _ in
      image.draw(in: CGRect(origin: .zero, size: image.size))
    }
  }
}
