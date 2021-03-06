package com.omega_r.libs.omegatypes

import android.app.Activity
import android.content.Context
import android.text.Html
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import java.util.*

open class Text(protected val defaultTextStyle: TextStyle?) : Serializable, Textable {


    companion object {
        @JvmStatic
        fun empty(): Text = Text(null)

        @JvmStatic
        @JvmOverloads
        fun from(string: String, textStyle: TextStyle? = null): Text = StringText(string, textStyle)


        @JvmStatic
        @JvmOverloads
        fun from(charSequence: CharSequence, textStyle: TextStyle? = null): Text = CharSequenceText(charSequence, textStyle)

        @JvmStatic
        @JvmOverloads
        fun from(stringRes: Int, textStyle: TextStyle? = null): Text = ResourceText(stringRes, textStyle)

        @JvmStatic
        @JvmOverloads
        fun from(stringRes: Int, vararg formatArgs: Any, textStyle: TextStyle? = null): Text = FormatResourceText(stringRes, *formatArgs, textStyle = textStyle)

        @JvmStatic
        @JvmOverloads
        fun fromPlurals(stringRes: Int, quantity: Int, vararg formatArgs: Any, textStyle: TextStyle? = null): Text = PluralsText(stringRes, quantity, *formatArgs, textStyle = textStyle)

        @JvmStatic
        @JvmOverloads
        fun from(stringHolder: StringHolder, textStyle: TextStyle? = null): Text = stringHolder.getStringText()?.let { from(it, textStyle) }
                ?: empty()

        @JvmStatic
        @JvmOverloads
        fun from(throwable: Throwable, textStyle: TextStyle? = null): Text = StringText(throwable.message, textStyle)

        @JvmStatic
        @JvmOverloads
        fun from(vararg texts: Text, textStyle: TextStyle? = null): Text = ArrayText(*texts, textStyle = textStyle)

        @JvmStatic
        @JvmOverloads
        fun from(texts: List<Text>, textStyle: TextStyle? = null): Text = ArrayText(*texts.toTypedArray(), textStyle = textStyle)

    }

    open fun isEmpty(): Boolean = true

    open fun getString(context: Context): String? = null

    @JvmOverloads
    open fun getCharSequence(context: Context, textStyle: TextStyle? = null): CharSequence? {
        return getString(context)?.let {
            (defaultTextStyle + textStyle)?.applyStyle(context, it) ?: it
        }
    }

    @JvmOverloads
    open fun applyTo(textView: TextView, textStyle: TextStyle? = null) {
        textView.text = getCharSequence(textView.context, textStyle)
    }

    open operator fun plus(text: Text): Text {
        return TextBuilder.BuilderText(this) + text
    }

    open operator fun plus(text: Textable): Text {
        return this + text.toText()
    }

    open operator fun plus(string: String): Text {
        return TextBuilder.BuilderText(this) + string
    }

    operator fun plus(textStyle: TextStyle?): Text {
        return textStyle?.let { from(this, textStyle = textStyle) } ?: this
    }

    override fun toText(): Text {
        return this
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Text

        return other.isEmpty()
    }

    override fun hashCode(): Int {
        return 31 * 17
    }


    private class StringText internal constructor(
            private val string: String?,
            textStyle: TextStyle?
    ) : Text(textStyle) {

        override fun isEmpty(): Boolean = string.isNullOrEmpty()

        override fun getString(context: Context): String? {
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


    private class CharSequenceText internal constructor(
            private var charSequence: CharSequence,
            textStyle: TextStyle?) : Text(textStyle) {

        companion object {
            private const val TYPE_SPANNED = 0.toByte()
            private const val TYPE_STRING = 1.toByte()
        }

        override fun isEmpty(): Boolean = charSequence.isEmpty()

        override fun getString(context: Context): String? {
            return charSequence.toString()
        }

        override fun getCharSequence(context: Context, textStyle: TextStyle?): CharSequence? {
            return (defaultTextStyle + textStyle)?.applyStyle(context, charSequence) ?: charSequence
        }

        @Throws(IOException::class)
        private fun writeObject(stream: ObjectOutputStream) {
            charSequence.let { charSequence->
                when (charSequence) {
                    is Spanned -> {
                        stream.writeByte(TYPE_SPANNED.toInt())
                        stream.writeObject(Html.toHtml(charSequence))
                    }
                    is String -> {
                        stream.writeByte(TYPE_STRING.toInt())
                        stream.writeUTF(charSequence)
                    }
                    else -> {
                        throw IOException("Unknown type " + charSequence::class.simpleName)
                    }
                }
            }
        }

        @Throws(IOException::class, ClassNotFoundException::class)
        private fun readObject(stream: ObjectInputStream) {
            charSequence = when (val type = stream.readByte()) {
                TYPE_SPANNED -> Html.fromHtml(stream.readObject() as String)
                TYPE_STRING -> stream.readUTF()
                else -> throw IOException("Unknown type = $type")
            }
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as CharSequenceText

            return charSequence == other.charSequence
        }

        override fun hashCode(): Int {
            var result = super.hashCode()
            result = 31 * result + charSequence.hashCode()
            return result
        }

    }

    private class ResourceText internal constructor(
            private val stringRes: Int,
            textStyle: TextStyle?

    ) : Text(textStyle) {

        override fun isEmpty(): Boolean = stringRes <= 0

        override fun getString(context: Context): String {
            return context.getString(stringRes)
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
                                                          private vararg val formatArgs: Any,
                                                          textStyle: TextStyle?) : Text(textStyle) {

        override fun isEmpty(): Boolean = stringRes <= 0

        override fun getString(context: Context): String {
            if (formatArgs.firstOrNull { it is Text } != null) {
                val list = formatArgs.map {
                    when (it) {
                        is Text -> it.getString(context)
                        else -> it
                    }
                }
                return context.getString(stringRes, *list.toTypedArray())
            }

            return context.getString(stringRes, *formatArgs)
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

    class PluralsText internal constructor(
            private val res: Int,
            private val quantity: Int,
            private vararg val formatArgs: Any,
            textStyle: TextStyle? = null
    ) : Text(textStyle) {

        override fun isEmpty(): Boolean = res <= 0

        override fun getString(context: Context): String? {
            return context.resources.getQuantityString(res, quantity, *formatArgs)
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            if (!super.equals(other)) return false

            other as PluralsText

            if (res != other.res) return false
            if (quantity != other.quantity) return false
            if (!formatArgs.contentEquals(other.formatArgs)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = super.hashCode()
            result = 31 * result + res
            result = 31 * result + quantity
            result = 31 * result + formatArgs.contentHashCode()
            return result
        }


    }

    private class ArrayText internal constructor(
            private vararg val texts: Text,
            textStyle: TextStyle?
    ) : Text(textStyle) {

        override fun isEmpty(): Boolean {
            if (texts.isEmpty()) return false
            for (text in texts) {
                if (!text.isEmpty()) {
                    return false
                }
            }
            return true
        }

        override fun getString(context: Context): String? {
            val stringBuilder = StringBuilder()
            for (text in texts) {
                stringBuilder.append(text.getString(context))
            }
            return stringBuilder.toString()
        }

        override fun getCharSequence(context: Context, textStyle: TextStyle?): CharSequence? {
            val stringBuilder = SpannableStringBuilder()
            val newTextStyle = defaultTextStyle + textStyle
            for (text in texts) {
                text.getCharSequence(context, newTextStyle)?.let { textCharSequence ->
                    stringBuilder.append(textCharSequence)
                }
            }
            return stringBuilder
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

interface Textable {

    fun toText(): Text

}


fun TextView.setText(text: Text?, textStyle: TextStyle? = null) {
    if (text == null) {
        this.text = null
    } else {
        text.applyTo(this, textStyle)
    }
}

fun EditText.setError(text: Text?, textStyle: TextStyle? = null) {
    this.error = text?.getCharSequence(context, textStyle)
}

fun EditText.setHint(text: Text?, textStyle: TextStyle? = null) {
    this.hint = text?.getCharSequence(context, textStyle)
}

fun Text?.applyTo(textView: TextView, textStyle: TextStyle? = null) {
    textView.setText(this, textStyle)
}

fun Text?.applyErrorTo(editText: EditText, textStyle: TextStyle? = null) {
    editText.setError(this, textStyle)
}

fun Activity.setTitle(text: Text?, textStyle: TextStyle? = null) {
    title = text?.getCharSequence(this, textStyle)
}

fun Context.toast(text: Text, duration: Int = Toast.LENGTH_SHORT, textStyle: TextStyle? = null): Toast {
    return Toast.makeText(this, text.getCharSequence(this, textStyle), duration).apply { show() }
}

fun String.toText(textStyle: TextStyle? = null) = Text.from(this, textStyle)

fun CharSequence.toText(textStyle: TextStyle? = null) = Text.from(this, textStyle)

operator fun Text?.plus(text: Text?): Text? {
    return when {
        this == null -> text
        text == null -> this
        else -> this + text
    }
}

operator fun Text?.plus(string: String?): Text? {
    return when {
        this == null -> string?.let { Text.from(string) }
        string == null -> this
        else -> this + string
    }
}

operator fun Text?.plus(textStyle: TextStyle?): Text? {
    return this?.let { this + textStyle }
}

fun List<Textable>.join(separator: String = ", ", prefix: String = "", postfix: String = "", limit: Int = -1, truncated: String = "..."): Text {
    var buffer = if (prefix.isNotEmpty()) Text.from(prefix) else Text.from()

    var count = 0

    for (element in this) {
        if (++count > 1) buffer += separator
        if (limit < 0 || count <= limit) {
            buffer += element
        } else break
    }

    if (limit in 0 until count) buffer += truncated

    if (postfix.isNotEmpty()) {
        buffer += postfix
    }

    return buffer
}