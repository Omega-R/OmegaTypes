package com.omega_r.libs.omegatypes.file

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

    suspend fun getMode(file: File): List<File.Mode>

    suspend fun createInputStream(file: File): InputStream?

    suspend fun createOutputStream(file: File, append: Boolean): OutputStream?

    suspend fun isExists(file: File): Boolean

    suspend fun getFiles(file: File): List<File>?

    suspend fun getRootFiles(fileClass: KClass<File>): List<File>?

    class Default : FileSystems {

        private val map: MutableMap<KClass<out File>, File.System<*>> = mutableMapOf()

        fun <F : File> addFileSystem(fileClass: KClass<F>, fileSystem: File.System<F>) {
            map[fileClass] = fileSystem
        }

        override suspend fun getMode(file: File): List<File.Mode> {
            return file.getFileSystem().getMode(file)
        }

        override suspend fun createInputStream(file: File): InputStream? {
            return file.getFileSystem().createInputStream(file)
        }

        override suspend fun createOutputStream(file: File, append: Boolean): OutputStream? {
            return file.getFileSystem().createOutputStream(file, append)
        }

        override suspend fun isExists(file: File): Boolean {
            return file.getFileSystem().isExists(file)
        }

        override suspend fun getFiles(file: File): List<File>? {
            return file.getFileSystem().getFiles(file)
        }

        override suspend fun getRootFiles(fileClass: KClass<File>): List<File>? {
            return getFileSystem(fileClass).getRootFiles()
        }

        private fun File.getFileSystem(): File.System<File> {
            return getFileSystem(this::class)
        }

        private fun getFileSystem(fileClass: KClass<out File>): File.System<File> {
            @Suppress("UNCHECKED_CAST")
            return map[fileClass] as? File.System<File>
                    ?: throw IllegalArgumentException("FileSystem not found for ${this::class}")

        }

    }

}