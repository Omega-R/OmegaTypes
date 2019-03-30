package com.omega_r.libs.omegatypes

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import java.io.Serializable
import java.util.*
import kotlin.text.StringBuilder

open class Text : Serializable {

    open fun isEmpty(): Boolean = true
    open fun getString(resources: Resources): String? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Text

        return other.isEmpty()
    }

    override fun hashCode(): Int {
        return 31 * 17
    }

    companion object {
        @JvmStatic
        fun empty(): Text = Text()

        @JvmStatic
        fun from(string: String): Text = StringText(string)

        @JvmStatic
        fun from(stringRes: Int): Text = ResourceText(stringRes)

        @JvmStatic
        fun from(stringRes: Int, vararg formatArgs: Any): Text = FormatResourceText(stringRes, *formatArgs)

        @JvmStatic
        fun from(stringHolder: StringHolder): Text = stringHolder.getStringText()?.let { StringText(it) } ?: empty()

        @JvmStatic
        fun from(throwable: Throwable): Text = StringText(throwable.message)

        @JvmStatic
        fun from(vararg texts: Text): Text = ArrayText(*texts)

        @JvmStatic
        fun from(texts: List<Text>): Text = ArrayText(*texts.toTypedArray())

    }

    private class StringText internal constructor(private val string: String?) : Text() {

        override fun isEmpty(): Boolean = string.isNullOrEmpty()

        override fun getString(resources: Resources): String? {
            return string
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as StringText

            return string == other.string
        }

        override fun hashCode(): Int {
            var result = super.hashCode()
            result = 31 * result + (string?.hashCode() ?: 0)
            return result
        }

    }

    private class ResourceText internal constructor(private val stringRes: Int) : Text() {

        override fun isEmpty(): Boolean = stringRes <= 0

        override fun getString(resources: Resources): String {
            return resources.getString(stringRes)
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as ResourceText

            return stringRes == other.stringRes
        }

        override fun hashCode(): Int {
            var result = super.hashCode()
            result = 31 * result + stringRes
            return result
        }

    }

    private class FormatResourceText internal constructor(private val stringRes: Int,
                                                          private vararg val formatArgs: Any) : Text() {

        override fun isEmpty(): Boolean = stringRes <= 0

        override fun getString(resources: Resources): String {
            return resources.getString(stringRes, *formatArgs)
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as FormatResourceText

            return stringRes == other.stringRes && Arrays.equals(formatArgs, other.formatArgs)
        }

        override fun hashCode(): Int {
            var result = super.hashCode() + stringRes
            result = 31 * result + Arrays.hashCode(formatArgs)
            return result
        }

    }

    private class ArrayText internal constructor(private vararg val texts: Text): Text() {

        override fun isEmpty(): Boolean {
            if (texts.isEmpty()) return false
            for (text in texts) {
                if (!text.isEmpty()) {
                    return false
                }
            }
            return true
        }

        override fun getString(resources: Resources): String? {
            val stringBuilder = StringBuilder()
            for (text in texts) {
                stringBuilder.append(text.getString(resources))
            }
            return stringBuilder.toString()
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            if (!super.equals(other)) return false

            other as ArrayText

            if (!texts.contentEquals(other.texts)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = super.hashCode()
            result = 31 * result + texts.contentHashCode()
            return result
        }


    }

    interface StringHolder {

        fun getStringText(): String?

    }

}

fun TextView.setText(text: Text) {
    this.text = text.getString(this.resources)
}

fun EditText.setError(text: Text) {
    this.error = text.getString(this.resources)
}

fun EditText.setHint(text: Text) {
    this.hint = text.getString(this.resources)
}

fun Text.applyTo(textView: TextView) {
    textView.setText(this)
}

fun Text.applyErrorTo(editText: EditText) {
    editText.setError(this)
}

fun Activity.setTitle(text: Text) {
    title = text.getString(resources)
}

fun Context.toast(text: Text, duration: Int = Toast.LENGTH_SHORT): Toast {
    return Toast.makeText(this, text.getString(resources), duration).apply { show() }
}

operator fun Text.plus(text: Text) : Text {
    if (this is TextBuilder.BuilderText) {
        return this + text
    }
    return TextBuilder.BuilderText(this) + text
}

operator fun Text.plus(string: String) : Text {
    if (this is TextBuilder.BuilderText) {
        return this + string
    }
    return TextBuilder.BuilderText(this) + string
}

fun String.toText() = Text.from(this)