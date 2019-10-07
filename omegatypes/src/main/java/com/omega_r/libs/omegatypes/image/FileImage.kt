package com.omega_r.libs.omegatypes.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.omega_r.libs.omegatypes.file.File
import com.omega_r.libs.omegatypes.file.FileSystems

/**
 * Created by Anton Knyazev on 2019-10-07.
 */
/**
 * Created by Anton Knyazev on 2019-10-03.
 */
data class FileImage(val file: File) : BaseBitmapImage() {

    companion object {

        init {
            ImageProcessors.default.addImageProcessor(FileImage::class, Processor())
        }

    }

    class Processor : BaseBitmapImage.Processor<FileImage>(true) {

        override suspend fun getBitmap(context: Context, image: FileImage, options: BitmapFactory.Options?): Bitmap? {
            val inputStream = FileSystems.current.createInputStream(context, image.file)
            return inputStream?.let { BitmapFactory.decodeStream(inputStream, null, options) }
        }

    }

}

fun Image.Companion.from(file: File) = FileImage(file)
