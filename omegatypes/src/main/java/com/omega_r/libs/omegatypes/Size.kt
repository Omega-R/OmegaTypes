package com.omega_r.libs.omegatypes

import android.content.Context
import android.util.DisplayMetrics
import android.util.TypedValue.*
import java.io.Serializable

/**
 * Created by Anton Knyazev on 18.05.2019.
 */
abstract class Size : Serializable {

    companion object {

        @JvmStatic
        fun from(size: Float, unit: Unit): Size = SimpleSize(size, unit)

        @JvmStatic
        fun from(dimensionRes: Int): Size = DimensionResourceSize(dimensionRes)

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

    protected fun getFactor(displayMetrics: DisplayMetrics, unit: Unit): Float {
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

    protected fun getFactor(displayMetrics: DisplayMetrics, oldUnit: Unit, newUnit: Unit): Float {
        if (oldUnit == newUnit) {
            return 1f
        }
        return getFactor(displayMetrics, newUnit) / getFactor(displayMetrics, oldUnit)
    }

    protected fun getFactor(context: Context, oldUnit: Unit, newUnit: Unit): Float {
        return context.resources.displayMetrics.run {
            getFactor(this, newUnit) / getFactor(this, oldUnit)
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

        override fun getSize(context: Context, unit: Unit) = context.resources.run {
            getDimension(sizeRes).applyFactor(context, Unit.PX, unit)
        }

        override fun getRawSize(context: Context) = context.resources.getDimension(sizeRes)

    }
}

