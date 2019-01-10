package com.omega_r.libs.omegatypes.picasso

import java.io.InputStream

/**
 * Created by Anton Knyazev on 28.12.18.
 */
internal class WrapperInputStream : InputStream() {

    internal var inputStream: InputStream? = null
        set(value) {
            field = value
            availableInitValue = 0
            synchronized(o) {
                o.notify()
            }
        }

    private val o = Object()

    private var availableInitValue = 1

    override fun read(): Int {
        if (inputStream == null) {
            synchronized(o) {
                o.wait()
            }
        }
        val inputStream = inputStream ?: return -1
        return inputStream.read()
    }

    override fun available(): Int {
        val inputStream = inputStream ?: return availableInitValue
        return inputStream.available()
    }

}