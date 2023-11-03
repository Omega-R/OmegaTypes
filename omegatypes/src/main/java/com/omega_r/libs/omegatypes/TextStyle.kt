package com.omega_r.libs.omegatypes

import android.content.Context
import android.graphics.Typeface
import android.text.InputFilter
import android.text.SpannableString
import android.text.Spanned
import android.text.TextUtils
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.text.style.LeadingMarginSpan
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.TypefaceSpan
import android.text.style.UnderlineSpan
import com.omega_r.libs.omegatypes.tools.TypefaceSpanCompat
import java.io.Serializable
import java.util.Locale

/**
 * Created by Anton Knyazev on 25.04.2019.
 */
abstract class TextStyle : Serializable {

    companion object {

        private val normalTextStyle = FontStyleTextStyle(Typeface.NORMAL)

        private val boldTextStyle = FontStyleTextStyle(Typeface.BOLD)

        private val italicTextStyle = FontStyleTextStyle(Typeface.ITALIC)

        private val underlineTextStyle = UnderlineTextStyle()

        private val strikethroughTextStyle = StrikethroughTextStyle()

        @JvmStatic
        fun color(color: Color): TextStyle = ColorTextStyle(color)

        @JvmStatic
        fun colorFromInt(colorInt: Int): TextStyle = ColorTextStyle(Color.fromInt(colorInt))

        @JvmStatic
        fun colorFromResource(colorRes: Int): TextStyle = ColorTextStyle(Color.fromResource(colorRes))

        @JvmStatic
        fun normal(): TextStyle = normalTextStyle

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

        @JvmStatic
        fun leadingMargin(firstMargin: Size, nextMargin: Size): TextStyle = LeadingMarginTextStyle(firstMargin, nextMargin)

        @JvmStatic
        fun paragraphMargin(margin: Size): TextStyle = leadingMargin(margin, Size.ZERO)

        @JvmStatic
        fun filter(filter: InputFilter): TextStyle = FilterTextStyle(filter)

        @JvmStatic
        fun uppercase(): TextStyle = filter(InputFilter.AllCaps())

        @JvmStatic
        fun lowercase(): TextStyle = filter(LowercaseInputFilter())

        @JvmStatic
        fun capitalize(): TextStyle = filter(CapitalizeInputFilter())

        @JvmStatic
        fun decapitalize(): TextStyle = filter(DecapitalizeInputFilter())

        @JvmStatic
        fun length(max: Int): TextStyle = filter(InputFilter.LengthFilter(max))
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

    open fun applyStyle(context: Context, charSequence: CharSequence): CharSequence {
        return getSpannableString(charSequence).apply {
            applyStyle(context)
        }
    }

    protected abstract fun SpannableString.applyStyle(context: Context)

    protected open fun getSpannableString(charSequence: CharSequence): SpannableString {
        return SpannableString.valueOf(charSequence)
    }

    private class Array(internal vararg val textStyleArray: TextStyle) : TextStyle() {

        override fun applyStyle(context: Context, charSequence: CharSequence): CharSequence {
            var result = charSequence
            textStyleArray.forEach {
                result = it.applyStyle(context, result)
            }
            return result
        }

        override fun SpannableString.applyStyle(context: Context) {
            // nothing
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

    private class LeadingMarginTextStyle(private val firstLineMargin: Size, private val nextLinesMargin: Size) : TextStyle() {

        override fun SpannableString.applyStyle(context: Context) {
            val firstMargin = firstLineMargin.getPixelOffset(context)
            val nextMargin = nextLinesMargin.getPixelOffset(context)
            setSpan(LeadingMarginSpan.Standard(firstMargin, nextMargin), 0, length, 0)
        }
    }

    private class FilterTextStyle(private val filter: InputFilter) : TextStyle() {

        override fun SpannableString.applyStyle(context: Context) {
            // nothing
        }

        override fun getSpannableString(charSequence: CharSequence): SpannableString {
            val spannableString = super.getSpannableString(charSequence)
            val result = filter.filter(charSequence, 0, charSequence.length, spannableString, 0, charSequence.length)
                ?: charSequence
            return SpannableString.valueOf(result)
        }
    }

    private class LowercaseInputFilter : InputFilter {

        override fun filter(source: CharSequence, start: Int, end: Int, dest: Spanned?, dstart: Int, dend: Int): CharSequence? {

            for (i in start until end) {
                if (source[i].isUpperCase()) {
                    val v = CharArray(end - start)
                    TextUtils.getChars(source, start, end, v, 0)
                    val s = String(v).lowercase(Locale.getDefault())

                    return if (source is Spanned) {
                        val sp = SpannableString(s)
                        TextUtils.copySpansFrom(source, start, end, null, sp, 0)
                        sp
                    } else {
                        s
                    }
                }
            }

            return null // keep original
        }
    }

    private class CapitalizeInputFilter : InputFilter {

        override fun filter(source: CharSequence, start: Int, end: Int, dest: Spanned?, dstart: Int, dend: Int): CharSequence? {
            val firstChar = source[start]
            if (firstChar.isLowerCase()) {
                val v = CharArray(end - start)
                TextUtils.getChars(source, start, end, v, 0)
                val s = String(v).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

                return if (source is Spanned) {
                    val sp = SpannableString(s)
                    TextUtils.copySpansFrom(source, start, end, null, sp, 0)
                    sp
                } else {
                    s
                }
            }

            return null // keep original
        }
    }

    private class DecapitalizeInputFilter : InputFilter {

        override fun filter(source: CharSequence, start: Int, end: Int, dest: Spanned?, dstart: Int, dend: Int): CharSequence? {
            val firstChar = source[start]
            if (firstChar.isUpperCase()) {
                val v = CharArray(end - start)
                TextUtils.getChars(source, start, end, v, 0)
                val s = String(v).replaceFirstChar { it.lowercase(Locale.getDefault()) }

                return if (source is Spanned) {
                    val sp = SpannableString(s)
                    TextUtils.copySpansFrom(source, start, end, null, sp, 0)
                    sp
                } else {
                    s
                }
            }

            return null // keep original
        }
    }

}

operator fun TextStyle?.plus(textStyle: TextStyle?): TextStyle? {
    return if (this == null) textStyle else this + textStyle
}