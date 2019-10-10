package com.omega_r.libs.omegatypes.image

import android.content.Context
import android.graphics.Bitmap
import com.omega_r.libs.omegatypes.decoders.toBitmap

/**
 * Created by Anton Knyazev on 2019-10-03.
 */
data class ByteArrayImage(val byteArray: ByteArray) : BaseBitmapImage() {

    companion object {

        init {
            ImageProcessors.default.addImageProcessor(ByteArrayImage::class, Processor())
        }

    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as ByteArrayImage

        if (!byteArray.contentEquals(other.byteArray)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + byteArray.contentHashCode()
        return result
    }

    open class Processor : BaseBitmapImage.Processor<ByteArrayImage>(true) {

        override suspend fun getBitmap(context: Context, image: ByteArrayImage, width: Int?, height: Int?): Bitmap? {
            return image.byteArray.toBitmap(width, height)
        }

    }

}

fun Image.Companion.from(byteArray: ByteArray) = ByteArrayImage(byteArray)
