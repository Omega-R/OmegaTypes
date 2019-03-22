package com.omega_r.libs.omegatypes

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import java.io.Serializable
import java.util.*

open class Text private constructor() : Serializable {

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
            return 31 * 17 + (string?.hashCode() ?: 0)
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
            return 31 * 17 + stringRes
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
            var result = 31 * 17 + stringRes
            result = 31 * result + Arrays.hashCode(formatArgs)
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