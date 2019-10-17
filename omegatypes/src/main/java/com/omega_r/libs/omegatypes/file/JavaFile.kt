package com.omega_r.libs.omegatypes.file

import android.content.Context
import android.webkit.MimeTypeMap
import com.omega_r.libs.omegatypes.file.File.System.Companion.getMode
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.*

/**
 * Created by Anton Knyazev on 2019-10-04.
 */
data class JavaFile(val file: java.io.File) : File {

    companion object {

        init {
            FileSystems.default.addFileSystem(JavaFile::class, System())
        }

    }

    override val name: String
        get() = file.name

    override val mimeType: String
        get() = MimeTypeMap.getFileExtensionFromUrl(file.toString())
                ?.apply { MimeTypeMap.getSingleton().getMimeTypeFromExtension(toLowerCase(Locale.US)) }
                ?: "*/*"

    override val type: File.Type
        get() = if (file.isFile) File.Type.FILE else File.Type.FOLDER

    class System : File.System<JavaFile> {

        companion object {
            private val rootFile = java.io.File("/")
        }

        override suspend fun getMode(context: Context, file: JavaFile): List<File.Mode> {
            val canRead = file.file.canRead()
            val canWrite = file.file.canWrite()

            return getMode(canRead, canWrite)
        }

        override suspend fun isExists(context: Context, file: JavaFile): Boolean {
            return file.file.exists()
        }

        override suspend fun createInputStream(context: Context, file: JavaFile): InputStream? {
            if (file.file.canRead()) {
                return file.file.inputStream()
            }
            return null
        }

        override suspend fun createOutputStream(context: Context, file: JavaFile, append: Boolean): OutputStream? {
            if (file.file.canWrite()) {
                return FileOutputStream(file.file, append)
            }
            return null
        }


        override suspend fun getFiles(context: Context, file: JavaFile): List<JavaFile>? {
            if (file.type == File.Type.FOLDER) {
                return file.file.listFiles().map(File.Companion::from)
            }
            return null
        }

        override suspend fun getRootFiles(context: Context): List<JavaFile>? {
            return rootFile.listFiles().map(File.Companion::from)
        }

    }

}

fun File.Companion.from(file: java.io.File): JavaFile {
    return JavaFile(file)
}
