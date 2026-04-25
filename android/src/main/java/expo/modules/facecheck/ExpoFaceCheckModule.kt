package expo.modules.facecheck

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition
import expo.modules.kotlin.Promise
import expo.modules.kotlin.exception.CodedException
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.io.File
import java.io.InputStream

private const val DEFAULT_MIN_PIXEL_SIZE: Long = 500_000L
private const val DEFAULT_AREA_THRESHOLD: Double = 0.2

class ExpoFaceCheckModule : Module() {
  override fun definition() = ModuleDefinition {
    Name("ExpoFaceCheck")

    AsyncFunction("checkFace") { imageUri: String, options: Map<String, Any?>?, promise: Promise ->
      try {
        val minPixelSize: Long
        val areaThreshold: Double

        val rawMin = options?.get("minPixelSize")
        if (rawMin != null) {
          val value = (rawMin as? Number)?.toDouble()
          if (value == null || value.isNaN() || value.isInfinite() || value < 0) {
            promise.reject(CodedException("ERR_INVALID_OPTIONS", "minPixelSize must be a non-negative finite number", null))
            return@AsyncFunction
          }
          minPixelSize = value.toLong()
        } else {
          minPixelSize = DEFAULT_MIN_PIXEL_SIZE
        }

        val rawArea = options?.get("areaThreshold")
        if (rawArea != null) {
          val value = (rawArea as? Number)?.toDouble()
          if (value == null || value.isNaN() || value.isInfinite() || value < 0.0 || value > 1.0) {
            promise.reject(CodedException("ERR_INVALID_OPTIONS", "areaThreshold must be a number between 0 and 1", null))
            return@AsyncFunction
          }
          areaThreshold = value
        } else {
          areaThreshold = DEFAULT_AREA_THRESHOLD
        }

        val context = appContext.reactContext ?: run {
          promise.reject(CodedException("ERR_NO_CONTEXT", "React context is not available", null))
          return@AsyncFunction
        }

        val uri = Uri.parse(imageUri)

        fun openStream(): InputStream? = when (uri.scheme) {
          "file", null -> File(uri.path ?: imageUri).inputStream()
          "content" -> context.contentResolver.openInputStream(uri)
          else -> File(imageUri).inputStream()
        }

        val decoded = openStream()?.use { BitmapFactory.decodeStream(it) }
        if (decoded == null) {
          promise.reject(CodedException("ERR_LOAD_IMAGE", "Failed to decode image: $imageUri", null))
          return@AsyncFunction
        }

        val orientation = openStream()?.use { ExifInterface(it).getAttributeInt(
          ExifInterface.TAG_ORIENTATION,
          ExifInterface.ORIENTATION_NORMAL
        ) } ?: ExifInterface.ORIENTATION_NORMAL

        val bitmap = applyExifOrientation(decoded, orientation)

        val imageWidth = bitmap.width.toLong()
        val imageHeight = bitmap.height.toLong()

        if (imageWidth * imageHeight < minPixelSize) {
          bitmap.recycle()
          promise.resolve(mapOf(
            "status" to "LOW_QUALITY",
            "faceCount" to 0
          ))
          return@AsyncFunction
        }

        val image = InputImage.fromBitmap(bitmap, 0)

        val detectorOptions = FaceDetectorOptions.Builder()
          .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
          .build()

        val detector = FaceDetection.getClient(detectorOptions)

        detector.process(image)
          .addOnSuccessListener { faces ->
            processFaces(faces, areaThreshold, promise)
            bitmap.recycle()
            detector.close()
          }
          .addOnFailureListener { e ->
            promise.reject(CodedException("ERR_FACE_DETECTION", "Face detection failed: ${e.message}", e))
            bitmap.recycle()
            detector.close()
          }
      } catch (e: Exception) {
        promise.reject(CodedException("ERR_FACE_DETECTION", "Face detection error: ${e.message}", e))
      }
    }
  }

  private fun applyExifOrientation(src: Bitmap, orientation: Int): Bitmap {
    val matrix = Matrix()
    when (orientation) {
      ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
      ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
      ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
      ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
      ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
      ExifInterface.ORIENTATION_TRANSPOSE -> { matrix.postRotate(90f); matrix.postScale(-1f, 1f) }
      ExifInterface.ORIENTATION_TRANSVERSE -> { matrix.postRotate(270f); matrix.postScale(-1f, 1f) }
      else -> return src
    }
    val rotated = Bitmap.createBitmap(src, 0, 0, src.width, src.height, matrix, true)
    if (rotated != src) src.recycle()
    return rotated
  }

  private fun processFaces(faces: List<Face>, areaThreshold: Double, promise: Promise) {
    data class DetectedFace(
      val x: Double,
      val y: Double,
      val width: Double,
      val height: Double,
      val area: Double
    )

    val detected = faces.map { face ->
      val bounds = face.boundingBox
      val w = bounds.width().toDouble()
      val h = bounds.height().toDouble()
      DetectedFace(
        x = bounds.left.toDouble(),
        y = bounds.top.toDouble(),
        width = w,
        height = h,
        area = kotlin.math.abs(w) * kotlin.math.abs(h)
      )
    }.sortedByDescending { it.area }

    if (detected.isEmpty()) {
      promise.resolve(mapOf(
        "status" to "NO_FACE",
        "faceCount" to 0
      ))
      return
    }

    val maxArea = detected[0].area
    val dominant = detected.filter { it.area / maxArea > areaThreshold }
    val dominantCount = dominant.size

    when {
      dominantCount == 0 -> promise.resolve(mapOf(
        "status" to "NO_FACE",
        "faceCount" to 0
      ))
      dominantCount == 1 -> {
        val top = detected[0]
        promise.resolve(mapOf(
          "status" to "READY",
          "faceCount" to 1,
          "dominantFaceBounds" to mapOf(
            "x" to top.x,
            "y" to top.y,
            "width" to top.width,
            "height" to top.height
          )
        ))
      }
      else -> promise.resolve(mapOf(
        "status" to "MULTIPLE_FACES",
        "faceCount" to dominantCount
      ))
    }
  }
}
