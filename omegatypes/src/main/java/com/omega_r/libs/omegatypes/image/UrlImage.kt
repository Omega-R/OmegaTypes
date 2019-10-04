package com.omega_r.libs.omegatypes.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.IOException
import java.lang.ref.WeakReference
import java.net.HttpURLConnection
import java.net.URL
import java.util.*


/**
 * Created by Anton Knyazev on 2019-10-03.
 */
data class UrlImage(val url: String) : BaseBitmapImage() {

    companion object {

        init {
            ImagesProcessor.default.addImageProcessor(UrlImage::class, Processor())
        }

    }

    class Processor : BaseBitmapImage.Processor<UrlImage>(true) {

        private var downloadedBytesMap = WeakHashMap<UrlImage, WeakReference<ByteArray>>()

        override fun getBitmap(context: Context, image: UrlImage, options: BitmapFactory.Options?): Bitmap? {

            return try {
                val saveWeek = options?.inJustDecodeBounds != false
                val bytes = image.getBytes(saveWeek)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options).also {
                    if (!saveWeek) {
                        downloadedBytesMap[image]?.clear()
                        downloadedBytesMap.remove(image)
                    }
                }
            } catch (e: IOException) {
                null
            }
        }


        private fun UrlImage.getBytes(saveWeak: Boolean): ByteArray {
            var readBytes = downloadedBytesMap[this]?.get()
            if (readBytes != null) {
                return readBytes
            }

            var connection: HttpURLConnection? = null

            try {
                connection = URL(url).openConnection() as HttpURLConnection
                connection.doInput = true;
                connection.connect()
                val input = connection.inputStream
                readBytes = input.readBytes()
                if (saveWeak) {
                    downloadedBytesMap[this] = WeakReference(readBytes)
                }
                return readBytes
            } finally {
                connection?.disconnect()
            }
        }


    }


}

fun Image.Companion.from(url: String) = UrlImage(url)
