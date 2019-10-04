package com.omega_r.libs.omegatypes.image

import android.content.ContentResolver.*
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream


/**
 * Created by Anton Knyazev on 2019-10-03.
 */
class UriImage private constructor() : BaseBitmapImage() {

    companion object {

        init {
            ImagesProcessor.default.addImageProcessor(UriImage::class, Processor())
        }

    }

    lateinit var uri: Uri
        private set

    constructor(uri: Uri) : this() {
        this.uri = uri
    }


    @Throws(IOException::class)
    private fun writeObject(outStream: ObjectOutputStream) {
        outStream.writeUTF(uri.toString())
    }

    @Throws(IOException::class, ClassNotFoundException::class)
    private fun readObject(inStream: ObjectInputStream) {
        uri = Uri.parse(inStream.readUTF())
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as UriImage

        if (uri != other.uri) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + uri.hashCode()
        return result
    }

    class Processor : BaseBitmapImage.Processor<UriImage>(true) {

        override fun getBitmap(context: Context, image: UriImage, options: BitmapFactory.Options?): Bitmap? {
            val uri = image.uri
            when (val scheme = uri.scheme) {
                SCHEME_ANDROID_RESOURCE, SCHEME_FILE, SCHEME_CONTENT -> {
                    val stream = context.contentResolver.openInputStream(uri) ?: return null
                    return BitmapFactory.decodeStream(stream, null, options)
                }
                else -> throw IllegalArgumentException("Not supported uri scheme = $scheme")
            }
        }

    }

}

fun Image.Companion.from(uri: Uri) = UriImage(uri)
