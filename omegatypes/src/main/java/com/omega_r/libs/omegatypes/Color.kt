package com.omega_r.libs.omegatypes

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.os.Build
import android.util.SparseIntArray
import android.util.TypedValue
import android.widget.TextView
import java.io.Serializable
import java.util.*


/**
 * Created by Anton Knyazev on 18.05.2019.
 */

private typealias GraphicColor = android.graphics.Color

abstract class Color : Serializable {

    abstract fun getColorInt(context: Context): Int

    open fun getColorStateList(context: Context): ColorStateList {
        return ColorStateList.valueOf(getColorInt(context))
    }

    companion object {

        val BLACK
            get() = fromInt(GraphicColor.BLACK)

        val WHITE
            get() = fromInt(GraphicColor.WHITE)

        val RED
            get() = fromInt(GraphicColor.RED)

        val DKGRAY
            get() = fromInt(GraphicColor.DKGRAY)

        val GRAY
            get() = fromInt(GraphicColor.GRAY)

        val GREEN
            get() = fromInt(GraphicColor.GREEN)

        val BLUE
            get() = fromInt(GraphicColor.BLUE)

        val YELLOW
            get() = fromInt(GraphicColor.YELLOW)

        val CYAN
            get() = fromInt(GraphicColor.CYAN)

        val MAGENTA
            get() = fromInt(GraphicColor.MAGENTA)

        val TRANSPARENT
            get() = fromInt(GraphicColor.TRANSPARENT)

        @JvmStatic
        fun fromInt(color: Int): Color = IntColor(color)

        @JvmStatic
        fun fromResource(colorRes: Int): Color = ResourceColor(colorRes)

        @JvmStatic
        fun fromAttribute(colorAttr: Int): Color = AttrThemeColor(colorAttr)

        @JvmStatic
        fun fromArgb(alpha: Int, red: Int, green: Int, blue: Int): Color = IntColor(GraphicColor.argb(alpha, red, green, blue))

        @JvmStatic
        fun fromString(colorString: String): Color = IntColor(GraphicColor.parseColor(colorString))

        @JvmStatic
        fun fromColorList(colorStateList: ColorStateList): Color = ColorStateListColor(colorStateList)

    }

    class IntColor(private val colorInt: Int) : Color() {
        override fun getColorInt(context: Context) = colorInt
    }

    class ResourceColor(private val colorRes: Int) : Color() {

        override fun getColorInt(context: Context): Int {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                context.getColor(colorRes)
            } else {
                context.resources.getColor(colorRes)
            }
        }

        override fun getColorStateList(context: Context): ColorStateList {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                context.getColorStateList(colorRes)
            } else {
                context.resources.getColorStateList(colorRes)
            }
        }
    }

    class AttrThemeColor(private val attrInt: Int) : Color() {

        companion object {

            private val cache = WeakHashMap<Resources.Theme, SparseIntArray>()

        }

        override fun getColorInt(context: Context): Int {

            val theme = context.theme

            val sparseIntArray = cache[theme]

            return sparseIntArray?.indexOfKey(attrInt)?.run {
                if (this >= 0) {
                    sparseIntArray.valueAt(this)
                } else {
                    sparseIntArray.extractAndPutCache(theme)
                }
            } ?: SparseIntArray().run {
                cache[theme] = this
                extractAndPutCache(theme)
            }
        }

        private fun SparseIntArray.extractAndPutCache(theme: Resources.Theme): Int {
            return extractColor(theme).apply {
                put(attrInt, this)
            }
        }

        private fun extractColor(theme: Resources.Theme): Int {
            return TypedValue().run {
                if (theme.resolveAttribute(attrInt, this, true)) data else 0
            }
        }

    }

    class ColorStateListColor(private val colorStateList: ColorStateList) : Color() {

        override fun getColorInt(context: Context): Int {
            return colorStateList.defaultColor
        }

        override fun getColorStateList(context: Context): ColorStateList {
            return colorStateList
        }

    }

}

var TextView.textColor: Color
    get() = Color.fromColorList(textColors)
    set(value) {
        setTextColor(value.getColorStateList(context))
    }

var TextView.hintTextColor: Color
    get() = Color.fromColorList(hintTextColors)
    set(value) {
        setHintTextColor(value.getColorStateList(context))
    }

var TextView.linkTextColor: Color
    get() = Color.fromColorList(linkTextColors)
    set(value) {
        setLinkTextColor(value.getColorStateList(context))
    }