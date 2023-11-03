package com.omega_r.libs.omegatypes.image

import android.content.Context
import android.graphics.Bitmap
import com.omega_r.libs.omegatypes.decoders.toBitmap

data class AssetImage(val fileName: String) : BaseBitmapImage() {

    companion object {

        init {
            ImageProcessors.default.addImageProcessor(AssetImage::class, Processor())
        }
    }

    class Processor : BaseBitmapImage.Processor<AssetImage>(true) {

        override suspend fun getBitmap(context: Context, image: AssetImage, width: Int?, height: Int?): Bitmap? {
            return context.assets.open(image.fileName).use {
                it.toBitmap(width, height)
            }
        }
    }
}
