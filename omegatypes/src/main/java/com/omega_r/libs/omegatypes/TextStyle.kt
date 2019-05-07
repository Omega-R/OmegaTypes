package com.omega_r.libs.omegatypes

import android.content.Context
import android.graphics.*
import android.os.Build
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import java.io.Serializable

/**
 * Created by Anton Knyazev on 25.04.2019.
 */
abstract class TextStyle : Serializable {

    companion object {
        fun color(colorRes: Int): TextStyle = Color(colorRes)

        fun bold(): TextStyle = TypeFaceStyle(Typeface.BOLD)

        fun italic(): TextStyle = TypeFaceStyle(Typeface.ITALIC)

    }

    operator fun plus(textStyle: TextStyle?):TextStyle {
        val list = mutableListOf<TextStyle>().apply {
            addTextStyle(this@TextStyle)
            addTextStyle(textStyle)
        }

        return Array(*list.toTypedArray())
    }

    private fun MutableList<TextStyle>.addTextStyle(textStyle: TextStyle?) {
        if (textStyle is Array) {
            addAll(textStyle.textStyleArray)
        } else {
            textStyle?.let { add(textStyle) }
        }
    }

    fun applyStyle(context: Context, charSequence: CharSequence): CharSequence {
        return getSpannableString(charSequence).apply {
            applyStyle(context)
        }
    }

    protected abstract fun SpannableString.applyStyle(context: Context)

    private fun getSpannableString(charSequence: CharSequence): SpannableString {
        return if (charSequence is SpannableString) charSequence else SpannableString(charSequence)
    }

    private class Array(internal vararg val textStyleArray: TextStyle) : TextStyle() {

        override fun SpannableString.applyStyle(context: Context) {
            textStyleArray.forEach {
                it.apply {
                    applyStyle(context)
                }
            }
        }
    }

    private class Color(private val colorRes: Int) : TextStyle() {

        override fun SpannableString.applyStyle(context: Context) {
            val color = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                context.getColor(colorRes)
            } else {
                context.resources.getColor(colorRes)
            }
            setSpan(ForegroundColorSpan(color), 0, length, 0)
        }

    }

    private class TypeFaceStyle(private val typeFaceStyle: Int) : TextStyle() {

        override fun SpannableString.applyStyle(context: Context) {
            setSpan(StyleSpan(typeFaceStyle), 0, length, 0)
        }
    }

}

operator fun TextStyle?.plus(textStyle: TextStyle?): TextStyle? {
    return if (this == null) textStyle else this + textStyle
}