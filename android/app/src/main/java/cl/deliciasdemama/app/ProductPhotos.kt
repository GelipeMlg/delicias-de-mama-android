package cl.deliciasdemama.app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import io.github.jan.supabase.storage.storage
import io.ktor.http.ContentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.UUID

/** Re-codifica y reduce la foto; el JPEG publicado no conserva metadatos de ubicación. */
object ProductPhotos {
    suspend fun upload(context: Context, uri: Uri, productId: String): String = withContext(Dispatchers.IO) {
        val resolver = context.contentResolver
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        require(bounds.outWidth > 0 && bounds.outHeight > 0) { "No se pudo leer la imagen. Elige otra foto." }
        var sample = 1
        while (maxOf(bounds.outWidth, bounds.outHeight) / sample > 1600) sample *= 2
        val bitmap = resolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample })
        } ?: throw IllegalArgumentException("No se pudo abrir la foto. Vuelve a seleccionarla.")
        try {
            val exif = resolver.openInputStream(uri)?.use { ExifInterface(it) }
            val transform = Matrix().apply {
                if (exif?.isFlipped == true) postScale(-1f, 1f)
                postRotate((exif?.rotationDegrees ?: 0).toFloat())
            }
            val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, transform, true)
            val bytes = try {
                ByteArrayOutputStream().use { out ->
                    check(rotated.compress(Bitmap.CompressFormat.JPEG, 85, out))
                    out.toByteArray()
                }
            } finally { if (rotated !== bitmap) rotated.recycle() }
            require(bytes.size <= 5 * 1024 * 1024) { "La foto es demasiado grande. Selecciona otra." }
            val bucket = SupabaseConnection.client.storage.from("productos")
            val path = "$productId/${UUID.randomUUID()}.jpg"
            bucket.upload(path, bytes) { contentType = ContentType.Image.JPEG; upsert = false }
            bucket.publicUrl(path)
        } finally { bitmap.recycle() }
    }
}
