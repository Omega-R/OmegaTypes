package com.omega_r.libs.omegatypes.decoders

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import java.io.BufferedInputStream
import java.io.File
import java.io.InputStream


/**
 * Created by Anton Knyazev on 2019-10-09.
 */
private const val MARK_POSITION = 5 * 1024 * 1024

open class SimpleBitmapDecoders(protected val bitmapPool: BitmapPool) : BitmapDecoders {

    override fun decodeBitmap(source: File, requiredWidth: Int?, requiredHeight: Int?): Bitmap? {
        return decodeBitmap(requiredWidth, requiredHeight) {
            BitmapFactory.decodeFile(source.absolutePath, it)
        }
    }

    override fun decodeBitmap(source: InputStream, requiredWidth: Int?, requiredHeight: Int?): Bitmap? {
        val stream = if (!source.markSupported()) BufferedInputStream(source) else source
        stream.mark(MARK_POSITION)

        return decodeBitmap(requiredWidth, requiredHeight) {
            BitmapFactory.decodeStream(stream, null, it).also {
                stream.reset()
            }
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

    protected inline fun decodeBitmap(reqWidth: Int?, reqHeight: Int?, decoder: (BitmapFactory.Options) -> Bitmap?): Bitmap? {
        val options = BitmapFactory.Options()

        if (reqWidth != null && reqHeight != null) {
            val tryOptions = BitmapFactory.Options()
            tryOptions.inJustDecodeBounds = true
            decoder(tryOptions)
            options.inSampleSize = calculateInSampleSize(tryOptions, reqWidth, reqHeight)
            tryOptions.inSampleSize = options.inSampleSize
            options.inMutable = true
            val inBitmap = bitmapPool.getBitmap(tryOptions.outWidth, tryOptions.outHeight, tryOptions.inPreferredConfig)

            if (inBitmap != null && canUseForInBitmap(inBitmap, tryOptions)) {
                options.inBitmap = inBitmap
            }
        } else {
            options.inMutable = true
            options.inSampleSize = 1
        }

        return try {
            val bitmap = decoder(options)
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            options.inBitmap ?.let {
                bitmapPool.putBitmap(it)
                options.inBitmap = null
            }

            val bitmap = decoder(options)
            bitmap
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