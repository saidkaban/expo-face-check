package expo.modules.facecheck

import android.graphics.BitmapFactory
import android.net.Uri
import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition
import expo.modules.kotlin.Promise
import expo.modules.kotlin.exception.CodedException
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.io.File

class ExpoFaceCheckModule : Module() {
  override fun definition() = ModuleDefinition {
    Name("ExpoFaceCheck")

    AsyncFunction("checkFace") { imageUri: String, promise: Promise ->
      try {
        val context = appContext.reactContext ?: run {
          promise.reject(CodedException("ERR_NO_CONTEXT", "React context is not available", null))
          return@AsyncFunction
        }

        val uri = Uri.parse(imageUri)

        // Load bitmap to get dimensions
        val inputStream = when (uri.scheme) {
          "file", null -> File(uri.path ?: imageUri).inputStream()
          "content" -> context.contentResolver.openInputStream(uri)
          else -> File(imageUri).inputStream()
        }

        if (inputStream == null) {
          promise.reject(CodedException("ERR_LOAD_IMAGE", "Failed to open image: $imageUri", null))
          return@AsyncFunction
        }

        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream.close()

        if (bitmap == null) {
          promise.reject(CodedException("ERR_LOAD_IMAGE", "Failed to decode image: $imageUri", null))
          return@AsyncFunction
        }

        val imageWidth = bitmap.width.toDouble()
        val imageHeight = bitmap.height.toDouble()
        val imageArea = imageWidth * imageHeight
        val minFaceArea = imageArea * 0.015 // 1.5% threshold

        val image = InputImage.fromBitmap(bitmap, 0)

        val options = FaceDetectorOptions.Builder()
          .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
          .build()

        val detector = FaceDetection.getClient(options)

        detector.process(image)
          .addOnSuccessListener { faces ->
            processFaces(faces, minFaceArea, promise)
            detector.close()
          }
          .addOnFailureListener { e ->
            promise.reject(CodedException("ERR_FACE_DETECTION", "Face detection failed: ${e.message}", e))
            detector.close()
          }
      } catch (e: Exception) {
        promise.reject(CodedException("ERR_FACE_DETECTION", "Face detection error: ${e.message}", e))
      }
    }
  }

  private fun processFaces(faces: List<Face>, minFaceArea: Double, promise: Promise) {
    data class SignificantFace(
      val x: Double,
      val y: Double,
      val width: Double,
      val height: Double,
      val area: Double
    )

    val significantFaces = faces.mapNotNull { face ->
      val bounds = face.boundingBox
      val faceWidth = bounds.width().toDouble()
      val faceHeight = bounds.height().toDouble()
      val faceArea = faceWidth * faceHeight

      if (faceArea >= minFaceArea) {
        SignificantFace(
          x = bounds.left.toDouble(),
          y = bounds.top.toDouble(),
          width = faceWidth,
          height = faceHeight,
          area = faceArea
        )
      } else null
    }.sortedByDescending { it.area }

    val faceCount = significantFaces.size

    when {
      faceCount == 0 -> {
        promise.resolve(mapOf(
          "status" to "NO_FACE",
          "faceCount" to 0
        ))
      }
      faceCount == 1 -> {
        val dominant = significantFaces[0]
        promise.resolve(mapOf(
          "status" to "READY",
          "faceCount" to 1,
          "dominantFaceBounds" to mapOf(
            "x" to dominant.x,
            "y" to dominant.y,
            "width" to dominant.width,
            "height" to dominant.height
          )
        ))
      }
      else -> {
        val largestArea = significantFaces[0].area
        val secondArea = significantFaces[1].area

        if (largestArea >= 2.0 * secondArea) {
          val dominant = significantFaces[0]
          promise.resolve(mapOf(
            "status" to "READY",
            "faceCount" to faceCount,
            "dominantFaceBounds" to mapOf(
              "x" to dominant.x,
              "y" to dominant.y,
              "width" to dominant.width,
              "height" to dominant.height
            )
          ))
        } else {
          promise.resolve(mapOf(
            "status" to "MULTIPLE_FACES",
            "faceCount" to faceCount
          ))
        }
      }
    }
  }
}
