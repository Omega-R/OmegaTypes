package com.omega_r.libs.omegatypes.decoders

import android.annotation.TargetApi
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import java.io.File
import java.io.InputStream

/**
 * Created by Anton Knyazev on 2019-10-08.
 */

typealias BitmapOptions = BitmapFactory.Options


interface BitmapDecoders {

    companion object {

        val default = Default()

        var current: BitmapDecoders = default

    }

    fun decodeBitmap(source: File, requiredWidth: Int? = null, requiredHeight: Int? = null): Bitmap?

    fun decodeBitmap(source: InputStream, requiredWidth: Int? = null, requiredHeight: Int? = null): Bitmap?

    fun decodeBitmap(source: ByteArray, requiredWidth: Int? = null, requiredHeight: Int? = null): Bitmap?

    fun recycle(bitmap: Bitmap)

    fun clearMemory()

    fun trimMemory(level: Int)

    fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        // Raw height and width of image
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {

            val halfHeight = height / 2
            val halfWidth = width / 2

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while (halfHeight / inSampleSize > reqHeight && halfWidth / inSampleSize > reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }


    @TargetApi(Build.VERSION_CODES.KITKAT)
    private fun getBitmapByteSize(bitmap: Bitmap): Int {
        // The return value of getAllocationByteCount silently changes for recycled bitmaps from the
        // internal buffer size to row bytes * height. To avoid random inconsistencies in caches, we
        // instead assert here.
        check(!bitmap.isRecycled) {
            ("Cannot obtain size for recycled Bitmap: " + bitmap
                    + "[" + bitmap.width + "x" + bitmap.height + "] " + bitmap.config)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // Workaround for KitKat initial release NPE in Bitmap, fixed in MR1. See issue #148.
            try {
                return bitmap.allocationByteCount
            } catch (e: NullPointerException) {
                // Do nothing.
            }

        }
        return bitmap.height * bitmap.rowBytes
    }

    fun getBitmapByteSize(width: Int, height: Int, config: Bitmap.Config): Int {
        return width * height * getBytesPerPixel(config)
    }

    fun getBytesPerPixel(config: Bitmap.Config?): Int {
        return when (config) {
            Bitmap.Config.ALPHA_8 -> 1
            Bitmap.Config.RGB_565, Bitmap.Config.ARGB_4444 -> 2
            Bitmap.Config.ARGB_8888 -> 4
            else -> 4
        }
    }

    class Default : BitmapDecoders {

        override fun decodeBitmap(source: File, requiredWidth: Int?, requiredHeight: Int?): Bitmap? {
            return decodeBitmap(requiredWidth, requiredHeight) {
                BitmapFactory.decodeFile(source.absolutePath, it)
            }
        }

        override fun decodeBitmap(source: InputStream, requiredWidth: Int?, requiredHeight: Int?): Bitmap? {
            return decodeBitmap(requiredWidth, requiredHeight) {
                BitmapFactory.decodeStream(source, null, it)
            }
        }

        override fun decodeBitmap(source: ByteArray, requiredWidth: Int?, requiredHeight: Int?): Bitmap? {
            return decodeBitmap(requiredWidth, requiredHeight) {
                BitmapFactory.decodeByteArray(source, 0, source.size, it)
            }
        }

        private inline fun decodeBitmap(reqWidth: Int?, reqHeight: Int?, bitmapFactory: (BitmapFactory.Options?) -> Bitmap?): Bitmap? {
            val options = if (reqWidth != null && reqHeight != null) {
                BitmapOptions().apply {
                    inJustDecodeBounds = true
                    bitmapFactory(this)
                    inJustDecodeBounds = false
                    inSampleSize = calculateInSampleSize(this, reqWidth, reqHeight)
                }
            } else null

            return bitmapFactory(options)

        }

        override fun recycle(bitmap: Bitmap) {
            bitmap.recycle()
        }

        override fun clearMemory() {
            // nothing
        }

        override fun trimMemory(level: Int) {
            // nothing
        }

    }

}

@JvmOverloads
fun File.toBitmap(
        requiredWidth: Int? = null,
        requiredHeight: Int? = null,
        decoders: BitmapDecoders = BitmapDecoders.current
): Bitmap? {
    return decoders.decodeBitmap(this, requiredWidth, requiredHeight)
}

@JvmOverloads
fun InputStream.toBitmap(
        requiredWidth: Int? = null,
        requiredHeight: Int? = null,
        decoders: BitmapDecoders = BitmapDecoders.current
): Bitmap? {
    return decoders.decodeBitmap(this, requiredWidth, requiredHeight)
}

@JvmOverloads
fun ByteArray.toBitmap(
        requiredWidth: Int? = null,
        requiredHeight: Int? = null,
        decoders: BitmapDecoders = BitmapDecoders.current
): Bitmap? {
    return decoders.decodeBitmap(this, requiredWidth, requiredHeight)
}