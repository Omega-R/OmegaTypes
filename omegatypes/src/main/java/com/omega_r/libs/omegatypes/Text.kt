package com.omega_r.libs.omegatypes

import android.content.res.Resources
import android.support.annotation.StringRes
import android.widget.EditText
import android.widget.TextView
import java.io.Serializable

interface Text : Serializable {

    fun isEmpty(): Boolean
    fun getString(resources: Resources): String?

    companion object {

        fun from(string: String): Text = StringText(string)

        fun from(@StringRes stringRes: Int): Text = ResourceText(stringRes)

        fun from(@StringRes stringRes: Int, vararg formatArgs: Any): Text =
                FormatResourceText(stringRes, *formatArgs)

        fun from(throwable: Throwable): Text = StringText(throwable.message)
    }

    class StringText constructor(private val string: String?) : Text {

        override fun isEmpty(): Boolean = string.isNullOrEmpty()

        override fun getString(resources: Resources): String? {
            return string
        }
    }

    class ResourceText internal constructor(@StringRes private val stringRes: Int) : Text {

        override fun isEmpty(): Boolean = stringRes <= 0

        override fun getString(resources: Resources): String {
            return resources.getString(stringRes)
        }

    }

    class FormatResourceText internal constructor(@StringRes private val stringRes: Int,
                                                  private vararg val formatArgs: Any) : Text {

        override fun isEmpty(): Boolean = stringRes <= 0

        override fun getString(resources: Resources): String {
            return resources.getString(stringRes, formatArgs)
        }
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