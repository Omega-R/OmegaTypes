package com.omega_r.libs.omegatypes

import java.io.Serializable
import java.lang.RuntimeException

open class Text : Serializable, Textable {

    companion object {

        @JvmStatic
        @JvmOverloads
        fun from(string: String, textStyle: TextStyle? = null): Text = throw Exception()

        @JvmStatic
        fun empty(): Text = throw Exception()

    }

    interface StringHolder {

        fun getStringText(): String?

    }

    operator fun plus(text: Text): Text = throw Exception()

    open operator fun plus(string: String): Text = throw Exception()

    open operator fun plus(text: Textable): Text = throw Exception()

    override fun toText(): Text = throw Exception()

}

abstract class TextStyle : Serializable

fun String.toText(textStyle: TextStyle? = null): Text = throw RuntimeException()

