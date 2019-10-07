package com.omega_r.libs.omegatypes.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

/**
 * Created by Anton Knyazev on 2019-10-03.
 */
class BitmapImage private constructor() : BaseBitmapImage() {

    companion object {

        init {
            ImageProcessors.default.addImageProcessor(BitmapImage::class, Processor())
        }

    }

    lateinit var bitmap: Bitmap
        private set

    constructor(bitmap: Bitmap): this() {
        this.bitmap = bitmap
    }

    override fun getDrawable(context: Context) = BitmapDrawable(context.resources, bitmap)

    @Throws(IOException::class)
    private fun writeObject(out: ObjectOutputStream) {

        val stream = ByteArrayOutputStream()
        if (bitmap.hasAlpha()) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        } else {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        }

        val byteArray = stream.toByteArray()
        out.writeInt(byteArray.size)
        out.write(byteArray)

    }

    @Throws(IOException::class, ClassNotFoundException::class)
    private fun readObject(inputStream: ObjectInputStream) {
        val bufferLength = inputStream.readInt()
        val stream = ByteArrayOutputStream(bufferLength)

        inputStream.copyTo(stream, bufferLength)

        bitmap = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, bufferLength)

    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as BitmapImage

        if (bitmap != other.bitmap) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + bitmap.hashCode()
        return result
    }


    class Processor : BaseBitmapImage.Processor<BitmapImage>(false) {

        override suspend fun getBitmap(context: Context, image: BitmapImage, options: BitmapFactory.Options?): Bitmap? {
            val bitmap = image.bitmap
            if (options?.inJustDecodeBounds == true) {
                options.outWidth = bitmap.width
                options.outHeight = bitmap.height
            }
            return bitmap
        }

    }

}

fun Image.Companion.from(bitmap: Bitmap) = BitmapImage(bitmap)
