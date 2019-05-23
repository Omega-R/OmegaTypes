package com.omega_r.libs.omegatypes

import android.content.Context
import android.content.res.ColorStateList
import android.os.Build
import java.io.Serializable

/**
 * Created by Anton Knyazev on 18.05.2019.
 */

abstract class Color  : Serializable {

    abstract fun getColorInt(context: Context): Int

    open fun getColorStateList(context: Context): ColorStateList {
        return ColorStateList.valueOf(getColorInt(context))
    }

    companion object {

        @JvmStatic
        fun fromInt(color: Int): Color = IntColor(color)

        @JvmStatic
        fun fromResource(colorRes: Int): Color = ResourceColor(colorRes)
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

}