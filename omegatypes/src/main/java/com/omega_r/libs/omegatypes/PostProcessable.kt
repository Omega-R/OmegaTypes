package com.omega_r.libs.omegatypes

import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.io.IOException

interface PostProcessable {
    fun onGsonPostProcess()

    class PostProcessingFactory : TypeAdapterFactory {
        override fun <T> create(gson: Gson, type: TypeToken<T>): TypeAdapter<T> {
            val delegate = gson.getDelegateAdapter(this, type)

            return object : TypeAdapter<T>() {
                @Throws(IOException::class)
                override fun write(writer: JsonWriter, value: T) {
                    delegate.write(writer, value)
                }

                @Throws(IOException::class)
                override fun read(reader: JsonReader): T {
                    val obj = delegate.read(reader)
                    (obj as? PostProcessable)?.onGsonPostProcess()
                    return obj
                }
            }
        }
    }
}
