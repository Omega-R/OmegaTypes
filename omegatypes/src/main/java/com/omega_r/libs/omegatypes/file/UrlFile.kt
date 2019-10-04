package com.omega_r.libs.omegatypes.file

import android.webkit.MimeTypeMap
import android.webkit.URLUtil
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

/**
 * Created by Anton Knyazev on 2019-10-04.
 */
class UrlFile(val url: String, name: String? = null, mimeType: String? = null) : File {

    companion object {

        init {
            FileSystems.default.addFileSystem(UrlFile::class, System())
        }

    }

    override val mimeType: String = mimeType ?: MimeTypeMap.getFileExtensionFromUrl(url)
            ?.apply { MimeTypeMap.getSingleton().getMimeTypeFromExtension(toLowerCase(Locale.US)) } ?: "*/*"

    override val name: String = name ?: URLUtil.guessFileName(url, null, mimeType)

    override val type: File.Type
        get() = File.Type.FILE

    class System : File.System<UrlFile> {

        override suspend fun getMode(file: UrlFile): List<File.Mode> = File.System.getMode(canRead = true, canWrite = false)

        override suspend fun createInputStream(file: UrlFile): InputStream? {
            var connection: HttpURLConnection? = null

            try {
                connection = URL(file.url).openConnection() as HttpURLConnection
                connection.doInput = true;
                connection.connect()

                val readBytes = connection.inputStream.readBytes()

                return readBytes.inputStream()

            } finally {
                connection?.disconnect()
            }
        }

        override suspend fun createOutputStream(file: UrlFile, append: Boolean): OutputStream? = null

        override suspend fun isExists(file: UrlFile): Boolean {
            val redirects = HttpURLConnection.getFollowRedirects()
            return try {
                HttpURLConnection.setFollowRedirects(false)
                val con = URL(file.url).openConnection() as HttpURLConnection
                con.requestMethod = "HEAD"
                con.responseCode == HttpURLConnection.HTTP_OK
            } catch (e: IOException) {
                e.printStackTrace()
                false
            } finally {
                HttpURLConnection.setFollowRedirects(redirects)
            }

        }

        override suspend fun getFiles(file: UrlFile): List<UrlFile>? = null

        override suspend fun getRootFiles(): List<UrlFile>? = null

    }

}