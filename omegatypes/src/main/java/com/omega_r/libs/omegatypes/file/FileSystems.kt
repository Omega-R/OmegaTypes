package com.omega_r.libs.omegatypes.file

import android.content.Context
import java.io.InputStream
import java.io.OutputStream
import kotlin.reflect.KClass

/**
 * Created by Anton Knyazev on 2019-10-04.
 */
interface FileSystems {

    companion object {

        val default = Default()

        var current: FileSystems = default

    }

    suspend fun getMode(context: Context, file: File): List<File.Mode>

    suspend fun createInputStream(context: Context, file: File): InputStream?

    suspend fun createOutputStream(context: Context, file: File, append: Boolean): OutputStream?

    suspend fun isExists(context: Context, file: File): Boolean

    suspend fun getFiles(context: Context, file: File): List<File>?

    suspend fun getRootFiles(context: Context, fileClass: KClass<File>): List<File>?

    class Default : FileSystems {

        private val map: MutableMap<KClass<out File>, File.System<*>> = mutableMapOf()

        fun <F : File> addFileSystem(fileClass: KClass<F>, fileSystem: File.System<F>) {
            map[fileClass] = fileSystem
        }

        override suspend fun getMode(context: Context, file: File): List<File.Mode> {
            return file.system.getMode(context, file)
        }

        override suspend fun createInputStream(context: Context, file: File): InputStream? {
            return file.system.createInputStream(context, file)
        }

        override suspend fun createOutputStream(context: Context, file: File, append: Boolean): OutputStream? {
            return file.system.createOutputStream(context, file, append)
        }

        override suspend fun isExists(context: Context, file: File): Boolean {
            return file.system.isExists(context, file)
        }

        override suspend fun getFiles(context: Context, file: File): List<File>? {
            return file.system.getFiles(context, file)
        }

        override suspend fun getRootFiles(context: Context, fileClass: KClass<File>): List<File>? {
            return getFileSystem(fileClass).getRootFiles(context)
        }

        private val File.system: File.System<File>
            get() = getFileSystem(this::class)

        private fun getFileSystem(fileClass: KClass<out File>): File.System<File> {
            @Suppress("UNCHECKED_CAST")
            return map[fileClass] as? File.System<File>
                    ?: throw IllegalArgumentException("FileSystem not found for ${this::class}")

        }

    }

}