package com.omega_r.libs.omegatypes.file

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import android.webkit.URLUtil
import java.io.*
import java.util.*

/**
 * Created by Anton Knyazev on 2019-10-04.
 */
class UriFile private constructor() : File {

    companion object {

        fun setAsDefaultUriFileSystem(context: Context) {
            FileSystems.default.addFileSystem(UriFile::class, UriFile.System(context))
        }

    }

    lateinit var uri: Uri
        private set

    override lateinit var name: String
        private set

    override lateinit var mimeType: String
        private set

    override val type: File.Type
        get() = File.Type.FILE

    constructor(uri: Uri, name: String?, mimeType: String?) : this() {
        this.uri = uri
        this.name = name ?: URLUtil.guessFileName(uri.toString(), null, mimeType)
        this.mimeType = mimeType ?: MimeTypeMap.getFileExtensionFromUrl(uri.toString())
                ?.apply { MimeTypeMap.getSingleton().getMimeTypeFromExtension(toLowerCase(Locale.US)) } ?: "*/*"
    }

    @Throws(IOException::class)
    private fun writeObject(outStream: ObjectOutputStream) {
        outStream.writeUTF(uri.toString())
        outStream.writeUTF(name)
        outStream.writeUTF(mimeType)
    }

    @Throws(IOException::class, ClassNotFoundException::class)
    private fun readObject(inStream: ObjectInputStream) {
        uri = Uri.parse(inStream.readUTF())
        name = inStream.readUTF()
        mimeType = inStream.readUTF()
    }

    class System(private val context: Context) : File.System<UriFile> {

        override suspend fun getMode(file: UriFile): List<File.Mode> = File.System.getMode(canRead = true, canWrite = false)

        override suspend fun createInputStream(file: UriFile): InputStream? {
            return context.contentResolver.openInputStream(file.uri) ?: return null
        }

        override suspend fun createOutputStream(file: UriFile, append: Boolean): OutputStream? = null

        override suspend fun isExists(file: UriFile): Boolean {
            try {
                createInputStream(file)?.close() ?: return false
                return true
            } catch (e: FileNotFoundException) {
                return false
            }
        }

        override suspend fun getFiles(file: UriFile): List<UriFile>? = null

        override suspend fun getRootFiles(): List<UriFile>? = null

    }


}