package com.omega_r.libs.omegatypes.image

import android.graphics.Bitmap
import com.omega_r.libs.omegatypes.decoders.SimpleBitmapDecoders

/**
 * Created by Anton Knyazev on 2019-10-09.
 */
class GlideBitmapPool(private val bitmapPool: com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool) : SimpleBitmapDecoders.BitmapPool {

//    companion object {
//
//    }

    override fun getBitmap(outWidth: Int, outHeight: Int, inPreferredConfig: Bitmap.Config?): Bitmap? {
        return bitmapPool.get(outWidth, outHeight, inPreferredConfig)
    }

    override fun putBitmap(bitmap: Bitmap) {
        return bitmapPool.put(bitmap)
    }

    override fun clearMemory() {
        bitmapPool.clearMemory()
    }

    override fun trimMemory(level: Int) {
        bitmapPool.trimMemory(level)
    }

}