package com.omega_r.libs.omegatypes

import java.io.Serializable
import java.lang.RuntimeException

interface Text : Serializable, Textable {

    interface StringHolder {

        fun getStringText(): String?

    }

}

abstract class TextStyle : Serializable

fun String.toText(textStyle: TextStyle? = null): Text = throw RuntimeException()