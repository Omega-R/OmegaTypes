package com.omega_r.libs.omegatypes

import android.content.Context
import android.content.res.Resources
import android.util.DisplayMetrics
import android.util.SparseArray
import android.util.SparseIntArray
import android.util.TypedValue
import android.util.TypedValue.*
import android.view.ViewGroup.LayoutParams
import android.widget.TextView
import java.io.Serializable
import java.util.*

/**
 * Created by Anton Knyazev on 18.05.2019.
 */
abstract class Size : Serializable {

    companion object {

        val ZERO = 0.px

        @JvmStatic
        fun from(size: Float, unit: Unit): Size = SimpleSize(size, unit)

        @JvmStatic
        fun from(size: Int, unit: Unit): Size = from(size.toFloat(), unit)

        @JvmStatic
        fun from(size: Double, unit: Unit): Size = from(size.toFloat(), unit)

        @JvmStatic
        fun from(dimensionRes: Int): Size = DimensionResourceSize(dimensionRes)

        @JvmStatic
        fun fromAttribute(dimensionAttr: Int): Size = AttrThemeSize(dimensionAttr)
    }

    abstract fun getSize(context: Context, unit: Unit): Float

    protected abstract fun getRawSize(context: Context): Float

    fun getPixelOffset(context: Context): Int {
        return getSize(context, Unit.PX).toInt()
    }

    fun getPixelSize(context: Context): Int {
        val f = getSize(context, Unit.PX)

//        A size conversion involves rounding the base value, and ensuring that a
//        non-zero base value is at least one pixel in size.
        val res = (if (f >= 0) f + 0.5f else f - 0.5f).toInt()
        if (res != 0) return res
        val value = getRawSize(context)
        if (value == 0f) return 0
        if (value > 0) return 1
        return -1
    }

    private fun getFactor(displayMetrics: DisplayMetrics, unit: Unit): Float {
        return displayMetrics.run {
            when (unit) {
                Unit.PX -> 1.0f
                Unit.DP -> density
                Unit.SP -> scaledDensity
                Unit.PT -> xdpi * (1.0f / 72)
                Unit.IN -> xdpi
                Unit.MM -> xdpi * (1.0f / 25.4f)
            }
        }
    }

    private fun getFactor(context: Context, oldUnit: Unit, newUnit: Unit): Float {
        return context.resources.displayMetrics.run {
            getFactor(this, oldUnit) / getFactor(this, newUnit)
        }
    }

    protected fun Float.applyFactor(context: Context, oldUnit: Unit, newUnit: Unit): Float {
        return this * getFactor(context, oldUnit, newUnit)
    }

    enum class Unit(val typedValueUnit: Int) {
        PX(COMPLEX_UNIT_PX),
        DP(COMPLEX_UNIT_DIP),
        SP(COMPLEX_UNIT_SP),
        PT(COMPLEX_UNIT_PT),
        IN(COMPLEX_UNIT_IN),
        MM(COMPLEX_UNIT_MM);

        companion object {

            @JvmStatic
            fun from(typedValueUnit: Int): Unit? {
                return values().firstOrNull { typedValueUnit == it.typedValueUnit }
            }
        }
    }

    private class SimpleSize(private val size: Float, private val unit: Unit) : Size() {

        override fun getRawSize(context: Context): Float = size

        override fun getSize(context: Context, unit: Unit): Float {
            return size.applyFactor(context, this.unit, unit)
        }
    }

    private class DimensionResourceSize(private val sizeRes: Int) : Size() {

        override fun getSize(context: Context, unit: Unit) = getRawSize(context).applyFactor(context, Unit.PX, unit)

        override fun getRawSize(context: Context) = context.resources.getDimension(sizeRes)
    }

    class AttrThemeSize(private val attrInt: Int) : Size() {

        companion object {

            private val cache = WeakHashMap<Resources.Theme, SparseArray<Float>>()
        }

        override fun getSize(context: Context, unit: Unit): Float =
            getRawSize(context).applyFactor(context, Unit.PX, unit)

        override fun getRawSize(context: Context): Float {
            val theme = context.theme
            val metrics = context.resources.displayMetrics
            val sparseIntArray = cache[theme]

            return (sparseIntArray?.indexOfKey(attrInt)?.run {
                if (this >= 0) {
                    sparseIntArray.valueAt(this)
                } else {
                    sparseIntArray.extractAndPutCache(metrics, theme)
                }
            } ?: SparseArray<Float>().run {
                cache[theme] = this
                extractAndPutCache(metrics, theme)
            })
        }

        private fun SparseArray<Float>.extractAndPutCache(metrics: DisplayMetrics, theme: Resources.Theme): Float {
            return extractSize(metrics, theme).apply {
                put(attrInt, this)
            }
        }

        private fun extractSize(metrics: DisplayMetrics, theme: Resources.Theme): Float {
            return TypedValue().run {
                if (theme.resolveAttribute(attrInt, this, true)) getDimension(metrics) else 0f
            }
        }
    }
}

fun TextView.setTextSize(size: Size) = setTextSize(COMPLEX_UNIT_PX, size.getSize(context, Size.Unit.PX))


val Int.px
    get() = Size.from(toFloat(), Size.Unit.PX)

val Float.px
    get() = Size.from(this, Size.Unit.PX)

val Double.px
    get() = Size.from(toFloat(), Size.Unit.PX)

val Int.dp
    get() = Size.from(toFloat(), Size.Unit.DP)

val Float.dp
    get() = Size.from(this, Size.Unit.DP)

val Double.dp
    get() = Size.from(toFloat(), Size.Unit.DP)

val Int.sp
    get() = Size.from(toFloat(), Size.Unit.SP)

val Float.sp
    get() = Size.from(this, Size.Unit.SP)

val Double.sp
    get() = Size.from(toFloat(), Size.Unit.SP)