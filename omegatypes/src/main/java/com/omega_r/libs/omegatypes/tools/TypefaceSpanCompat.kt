package com.omega_r.libs.omegatypes.tools

import android.graphics.Paint
import android.graphics.Typeface
import android.os.Parcel
import android.text.ParcelableSpan
import android.text.TextPaint
import android.text.style.MetricAffectingSpan
import android.text.style.TypefaceSpan

class TypefaceSpanCompat(
        var family: String? = null,
        var typeface: Typeface? = null) : MetricAffectingSpan(), ParcelableSpan {

    companion object {
        private const val SPAN_TYPE_ID = 13
    }

    /**
     * Constructs a [TypefaceSpan] from a  parcel.
     */
    constructor(src: Parcel) : this(family = src.readString(), typeface = LeakyTypefaceStorageCompat.readTypefaceFromParcel(src))

    override fun getSpanTypeId(): Int {
        return SPAN_TYPE_ID
    }

    override fun describeContents(): Int {
        return 0
    }

    fun getSpanTypeIdInternal(): Int {
        return 13
    }

    fun writeToParcelInternal(dest: Parcel, flags: Int) {
        dest.writeString(family)
        typeface?.let { LeakyTypefaceStorageCompat.writeTypefaceToParcel(it, dest) }
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        writeToParcelInternal(dest, flags)
    }

    override fun updateDrawState(ds: TextPaint) {
        updateTypeface(ds)
    }

    override fun updateMeasureState(paint: TextPaint) {
        updateTypeface(paint)
    }

    private fun updateTypeface(paint: Paint) {
        if (typeface != null) {
            paint.typeface = typeface
        } else if (family != null) {
            applyFontFamily(paint, family!!)
        }
    }

    private fun applyFontFamily(paint: Paint, family: String) {
        val style: Int
        val old = paint.typeface
        style = old?.style ?: Typeface.NORMAL
        val styledTypeface = Typeface.create(family, style)
        val fake = style and styledTypeface.style.inv()

        if (fake and Typeface.BOLD != 0) {
            paint.isFakeBoldText = true
        }

        if (fake and Typeface.ITALIC != 0) {
            paint.textSkewX = -0.25f
        }
        paint.typeface = styledTypeface
    }
}