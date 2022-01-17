package com.omega_r.libs.omegatypes

import android.content.Context
import android.content.res.Resources
import com.omega_r.libs.omegatypes.image.Image
import java.io.Serializable

/**
 * Created by Anton Knyazev on 29.03.2019.
 */
private const val DEFAULT_CAPACITY = 10
class TextBuilder(capacity: Int) : Serializable {

    private val list: MutableList<Text> = ArrayList(capacity)

    constructor(textList: List<Text>): this(textList.size + DEFAULT_CAPACITY) {
        list.addAll(textList)
    }

    constructor(vararg text: Text): this(text.size + DEFAULT_CAPACITY) {
        list.addAll(text)
    }

    constructor(): this(DEFAULT_CAPACITY)

    fun append(text: Text): TextBuilder {
        list += text
        return this
    }

    fun append(image: Image): TextBuilder = append(Text.from(image))

    fun append(string: String) = append(Text.from(string))

    fun append(stringRes: Int) = append(Text.from(stringRes))

    fun append(stringHolder: Text.StringHolder) = append(Text.from(stringHolder))

    fun append(throwable: Throwable) = append(Text.from(throwable))

    fun append(stringRes: Int, vararg formatArgs: Any) = append(Text.from(stringRes, formatArgs))

    fun append(vararg texts: Text) = append(Text.from(*texts))

    fun insert(text: Text, index: Int): TextBuilder {
        list.add(index, text)
        return this
    }

    fun toText(): Text {
        return Text.from(list)
    }

    fun isEmpty(): Boolean {
        for (text in list) {
            if (!text.isEmpty()) {
                return false
            }
        }
        return true
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TextBuilder

        if (list != other.list) return false

        return true
    }

    override fun hashCode(): Int {
        return list.hashCode()
    }


    class BuilderText(private val textBuilder: TextBuilder): Text(null) {

        constructor(text: Text) : this(TextBuilder(text))

        override fun isEmpty(): Boolean {
            if (textBuilder.list.isEmpty()) {
                return true
            }
            if (textBuilder.list.firstOrNull { !it.isEmpty() } != null) return false
            return true
        }

        override fun getString(context: Context): String? {
            return textBuilder.toText().getString(context)
        }

        override fun getCharSequence(context: Context, textStyle: TextStyle?): CharSequence? {
            return textBuilder.toText().getCharSequence(context, textStyle)
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            if (!super.equals(other)) return false

            other as BuilderText

            if (textBuilder != other.textBuilder) return false

            return true
        }

        override fun hashCode(): Int {
            var result = super.hashCode()
            result = 31 * result + textBuilder.hashCode()
            return result
        }

        fun insert(text: Text, index: Int): BuilderText {
            textBuilder.insert(text, index)
            return this
        }

        override operator fun plus(text: Text): Text {
            textBuilder.append(text)
            return this
        }

        override operator fun plus(string: String): Text {
            textBuilder.append(string)
            return this
        }
    }

}
