package com.omega_r.libs.omegatypes.file

import android.content.Context
import java.io.InputStream
import java.io.OutputStream
import java.io.Serializable

/**
 * Created by Anton Knyazev on 2019-10-04.
 */
interface File : Serializable {

    companion object {
        // for add extensions
    }

    val name: String

    val mimeType: String

    val type: Type

    enum class Mode {
        READ, WRITE
    }

    enum class Type {
        FILE, FOLDER
    }

    interface System<F : File> {

        companion object {

            private val MODE_READ_AND_WRITE = listOf(Mode.READ, Mode.WRITE)

            private val MODE_ONLY_READ = listOf(Mode.READ)

            private val MODE_ONLY_WRITE = listOf(Mode.WRITE)

            private val MODE_NONE = emptyList<Mode>()

            fun getMode(canRead: Boolean, canWrite: Boolean): List<Mode> {
                return when {
                    canRead && canWrite -> MODE_READ_AND_WRITE
                    canRead -> MODE_ONLY_READ
                    canWrite -> MODE_ONLY_WRITE
                    else -> MODE_NONE
                }
            }
        }

        suspend fun getMode(context: Context, file: F): List<Mode>

        suspend fun createInputStream(context: Context, file: F): InputStream?

        suspend fun createOutputStream(context: Context, file: F, append: Boolean = false): OutputStream?

        suspend fun isExists(context: Context, file: F): Boolean

        suspend fun getFiles(context: Context, file: F): List<F>?

        suspend fun getRootFiles(context: Context): List<F>?

    }

}