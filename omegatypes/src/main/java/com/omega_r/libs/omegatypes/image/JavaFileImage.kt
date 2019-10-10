package com.omega_r.libs.omegatypes.image

import android.content.Context
import android.graphics.Bitmap
import com.omega_r.libs.omegatypes.decoders.toBitmap
import java.io.File

/**
 * Created by Anton Knyazev on 2019-10-03.
 */
data class JavaFileImage(val file: File) : BaseBitmapImage() {

    companion object {

        init {
            ImageProcessors.default.addImageProcessor(JavaFileImage::class, Processor())
        }

    }

    class Processor : BaseBitmapImage.Processor<JavaFileImage>(true) {

        override suspend fun getBitmap(context: Context, image: JavaFileImage, width: Int?, height: Int?): Bitmap? {
            return image.file.toBitmap(width, height)
        }

    }

}

fun Image.Companion.from(file: File) = JavaFileImage(file)
