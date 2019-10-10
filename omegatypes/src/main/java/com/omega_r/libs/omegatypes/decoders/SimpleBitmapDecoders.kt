package com.omega_r.libs.omegatypes.decoders

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import java.io.File
import java.io.InputStream


/**
 * Created by Anton Knyazev on 2019-10-09.
 */
open class SimpleBitmapDecoders(protected val bitmapPool: BitmapPool) : BitmapDecoders {

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

    override fun recycle(bitmap: Bitmap) {
        bitmapPool.putBitmap(bitmap)
    }

    override fun clearMemory() {
        bitmapPool.clearMemory()
    }

    override fun trimMemory(level: Int) {
        bitmapPool.trimMemory(level)
    }

    protected inline fun decodeBitmap(reqWidth: Int?, reqHeight: Int?, bitmapFactory: (BitmapFactory.Options) -> Bitmap?): Bitmap? {
        val options = BitmapFactory.Options()

        val inBitmap = if (reqWidth != null && reqHeight != null) {
            options.inJustDecodeBounds = true
            bitmapFactory(options)
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
            options.inJustDecodeBounds = false
            options.inMutable = true
            bitmapPool.getBitmap(options.outWidth, options.outHeight, options.inPreferredConfig)
        } else {
            options.inMutable = true
            options.inSampleSize = 1
            null
        }

        if (inBitmap != null && canUseForInBitmap(inBitmap, options)) {
            options.inBitmap = inBitmap
        }

        return try {
            bitmapFactory(options)
        } catch (e: Exception) {
            options.inBitmap = null
            bitmapFactory(options)
        }
    }

    protected fun canUseForInBitmap(candidate: Bitmap, targetOptions: BitmapFactory.Options): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // From Android 4.4 (KitKat) onward we can re-use if the byte size of
            // the new bitmap is smaller than the reusable bitmap candidate
            // allocation byte count.
            val width = targetOptions.outWidth / targetOptions.inSampleSize
            val height = targetOptions.outHeight / targetOptions.inSampleSize
            val byteCount = width * height * getBytesPerPixel(candidate.config)

            try {
                return byteCount <= candidate.allocationByteCount
            } catch (e: NullPointerException) {
                return byteCount <= candidate.height * candidate.rowBytes
            }

        }
        // On earlier versions, the dimensions must match exactly and the inSampleSize must be 1
        return (candidate.width == targetOptions.outWidth
                && candidate.height == targetOptions.outHeight
                && targetOptions.inSampleSize == 1)
    }

    interface BitmapPool {

        fun getBitmap(outWidth: Int, outHeight: Int, inPreferredConfig: Bitmap.Config?): Bitmap?

        fun putBitmap(bitmap: Bitmap)

        fun clearMemory()

        fun trimMemory(level: Int)

    }

}