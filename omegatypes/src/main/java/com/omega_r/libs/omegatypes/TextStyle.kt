package com.omega_r.libs.omegatypes

import android.content.Context
import android.graphics.*
import android.text.SpannableString
import android.text.style.*
import java.io.Serializable
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan

/**
 * Created by Anton Knyazev on 25.04.2019.
 */
abstract class TextStyle : Serializable {

    companion object {

        private val boldTextStyle = FontStyleTextStyle(Typeface.BOLD)

        private val italicTextStyle = FontStyleTextStyle(Typeface.ITALIC)

        private val underlineTextStyle = UnderlineTextStyle()

        private val strikethroughTextStyle = StrikethroughTextStyle()

        @JvmStatic
        fun color(color: Color): TextStyle = ColorTextStyle(color)

        @JvmStatic
        fun color(colorInt: Int): TextStyle = ColorTextStyle(Color.fromInt(colorInt))

        @JvmStatic
        fun bold(): TextStyle = boldTextStyle

        @JvmStatic
        fun italic(): TextStyle = italicTextStyle

        @JvmStatic
        fun underline(): TextStyle = underlineTextStyle

        @JvmStatic
        fun strikethrough(): TextStyle = strikethroughTextStyle

        @JvmStatic
        fun font(fontName: Text): TextStyle = NameFontTextStyle(fontNameText = fontName)

        @JvmStatic
        fun font(fontName: String): TextStyle = NameFontTextStyle(fontNameText = fontName.toText())

        @JvmStatic
        fun font(typeface: Typeface): TextStyle = TypefaceFontTextStyle(fontTypeface = typeface)

        @JvmStatic
        fun size(size: Size): TextStyle = SizeTextStyle(size)

    }

    operator fun plus(textStyle: TextStyle?): TextStyle {
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

    private class ColorTextStyle(private val color: Color) : TextStyle() {

        override fun SpannableString.applyStyle(context: Context) {
            val color = color.getColorInt(context)
            setSpan(ForegroundColorSpan(color), 0, length, 0)
        }

    }

    private class FontStyleTextStyle(private val typeFaceStyle: Int) : TextStyle() {

        override fun SpannableString.applyStyle(context: Context) {
            setSpan(StyleSpan(typeFaceStyle), 0, length, 0)
        }
    }

    private class UnderlineTextStyle : TextStyle() {

        override fun SpannableString.applyStyle(context: Context) {
            setSpan(UnderlineSpan(), 0, length, 0)
        }
    }

    private class NameFontTextStyle(private val fontNameText: Text) : TextStyle() {

        override fun SpannableString.applyStyle(context: Context) {
            fontNameText.getString(context)?.let {
                setSpan(TypefaceSpan(it), 0, length, 0)
            }
        }

    }

    private class TypefaceFontTextStyle(private val fontTypeface: Typeface) : TextStyle() {

        override fun SpannableString.applyStyle(context: Context) {
            setSpan(TypefaceSpanCompat(typeface = fontTypeface), 0, length, 0)
        }

    }

    private class SizeTextStyle(private val size: Size) : TextStyle() {

        override fun SpannableString.applyStyle(context: Context) {
            setSpan(AbsoluteSizeSpan(size.getPixelSize(context), false), 0, length, 0)
        }

    }

    private class StrikethroughTextStyle : TextStyle() {

        override fun SpannableString.applyStyle(context: Context) {
            setSpan(StrikethroughSpan(), 0, length, 0)
        }

    }

}

operator fun TextStyle?.plus(textStyle: TextStyle?): TextStyle? {
    return if (this == null) textStyle else this + textStyle
}