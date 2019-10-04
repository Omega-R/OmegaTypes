package com.omega_r.libs.omegatypes.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File

/**
 * Created by Anton Knyazev on 2019-10-03.
 */
data class FileImage(val file: File) : BaseBitmapImage() {

    companion object {

        init {
            ImagesProcessor.default.addImageProcessor(FileImage::class, Processor())
        }

    }

    class Processor : BaseBitmapImage.Processor<FileImage>(true) {

        override fun getBitmap(context: Context, image: FileImage, options: BitmapFactory.Options?): Bitmap? {
            return BitmapFactory.decodeFile(image.file.absolutePath)
        }

    }

}

fun Image.Companion.from(file: File) = FileImage(file)
