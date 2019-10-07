package com.omega_r.libs.omegatypes.file

import android.content.Context
import android.graphics.Bitmap
import com.omega_r.libs.omegatypes.image.Image
import com.omega_r.libs.omegatypes.image.getStream
import java.io.InputStream
import java.io.OutputStream

/**
 * Created by Anton Knyazev on 2019-10-07.
 */
data class ImageFile(val image: Image,
                override val name: String,
                override val mimeType: String) : File {

    companion object {

        private const val MIME_TYPE_PNG = "image/png"

        init {
            FileSystems.default.addFileSystem(ImageFile::class, System())
        }

    }

    override val type: File.Type
        get() = File.Type.FILE


    class System : File.System<ImageFile> {

        override suspend fun getMode(context: Context, file: ImageFile): List<File.Mode> = File.System.getMode(canRead = true, canWrite = false)

        override suspend fun createInputStream(context: Context, file: ImageFile): InputStream? {
            return when (file.mimeType) {
                MIME_TYPE_PNG -> file.image.getStream(context, Bitmap.CompressFormat.PNG)
                else -> file.image.getStream(context)
            }
        }

        override suspend fun createOutputStream(context: Context, file: ImageFile, append: Boolean): OutputStream? = null

        override suspend fun isExists(context: Context, file: ImageFile): Boolean = true

        override suspend fun getFiles(context: Context, file: ImageFile): List<ImageFile>? = null

        override suspend fun getRootFiles(context: Context): List<ImageFile>? = null

    }


}