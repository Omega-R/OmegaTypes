package com.omega_r.libs.omegatypes.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory

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

        override suspend fun getBitmap(context: Context, image: ByteArrayImage, options: BitmapFactory.Options?): Bitmap? {
            return BitmapFactory.decodeByteArray(image.byteArray, 0, image.byteArray.size, options)
        }

    }

}

fun Image.Companion.from(byteArray: ByteArray) = ByteArrayImage(byteArray)
