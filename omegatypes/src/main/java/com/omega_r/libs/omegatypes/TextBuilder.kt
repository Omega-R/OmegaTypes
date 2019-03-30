package com.omega_r.libs.omegatypes

import android.content.res.Resources

/**
 * Created by Anton Knyazev on 29.03.2019.
 */
private const val DEFAULT_CAPACITY = 10
class TextBuilder(capacity: Int) {

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


    class BuilderText(private val textBuilder: TextBuilder): Text() {

        constructor(text: Text) : this(TextBuilder(text))

        override fun isEmpty(): Boolean {
            if (textBuilder.list.isEmpty()) {
                return true
            }
            if (textBuilder.list.firstOrNull { !it.isEmpty() } != null) return false
            return false
        }

        override fun getString(resources: Resources): String? {
            return textBuilder.toText().getString(resources)
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

        operator fun plus(text: Text): Text {
            textBuilder.append(text)
            return this
        }

        operator fun plus(string: String): Text{
            textBuilder.append(string)
            return this
        }


    }
}
