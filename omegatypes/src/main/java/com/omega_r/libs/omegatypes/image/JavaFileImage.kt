package com.omega_r.libs.omegatypes.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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

        override suspend fun getBitmap(context: Context, image: JavaFileImage, options: BitmapFactory.Options?): Bitmap? {
            return BitmapFactory.decodeFile(image.file.absolutePath, options)
        }

    }

}

fun Image.Companion.from(file: File) = JavaFileImage(file)
