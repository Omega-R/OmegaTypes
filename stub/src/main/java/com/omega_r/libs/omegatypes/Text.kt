package com.omega_r.libs.omegatypes

import java.io.Serializable
import java.lang.RuntimeException

open class Text : Serializable, Textable {

    companion object {

        @JvmStatic
        @JvmOverloads
        fun from(string: String, textStyle: TextStyle? = null): Text = throw Exception()

    }

    interface StringHolder {

        fun getStringText(): String?

    }

    operator fun plus(text: Text): Text = throw Exception()

    override fun toText(): Text = throw Exception()

}

abstract class TextStyle : Serializable

fun String.toText(textStyle: TextStyle? = null): Text = throw RuntimeException()

