package com.omega_r.libs.omegatypes.image

import android.content.Context
import android.graphics.Bitmap
import com.omega_r.libs.omegatypes.decoders.toBitmap
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.regex.Pattern


/**
 * Created by Anton Knyazev on 2019-10-03.
 */
data class UrlImage(val baseUrl: String? = null, val relativeUrl: String) : BaseBitmapImage() {

    companion object {
        private val PATTERN_ABSOLUTE_URL = Pattern.compile("\\A[a-z0-9.+-]+://.*", Pattern.CASE_INSENSITIVE)

        var defaultBaseUrl: String? = null

        init {
            ImageProcessors.default.addImageProcessor(UrlImage::class, Processor())
        }

        private fun String.isAbsoluteUrl(): Boolean = PATTERN_ABSOLUTE_URL.matcher(this).matches()

    }


    val url: String
        get() = if (relativeUrl.isAbsoluteUrl()) relativeUrl else {
            val baseUrl = (baseUrl ?: defaultBaseUrl ?: "").removeSuffix("/")
            val relativeUrl = relativeUrl.removePrefix("/")
            "$baseUrl/$relativeUrl"
        }

    constructor(url: String) : this(null, url)

    class Processor : BaseBitmapImage.Processor<UrlImage>(true) {


        override suspend fun getBitmap(context: Context, image: UrlImage, width: Int?, height: Int?): Bitmap? {

            var connection: HttpURLConnection? = null
            return try {
                connection = URL(image.url).openConnection() as HttpURLConnection
                connection.doInput = true;
                connection.connect()
                connection.inputStream
                        .toBitmap(width, height)
            } catch (e: IOException) {
                e.printStackTrace()
                null
            } finally {
                connection?.disconnect()
            }
        }

    }


}

fun Image.Companion.from(url: String) = UrlImage(url)
