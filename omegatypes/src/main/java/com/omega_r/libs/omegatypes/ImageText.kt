package com.omega_r.libs.omegatypes

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.DynamicDrawableSpan
import com.omega_r.libs.omegatypes.image.Image

class ImageText(private val image: Image) : Text(null) {

    override fun isEmpty(): Boolean = false

    override fun getString(context: Context): String = ""

    override fun getCharSequence(context: Context, textStyle: TextStyle?): CharSequence {
        val builder = SpannableStringBuilder(" ")

        val drawable = image.getDrawable(context)
        val span = ImageSpan(drawable ?: ColorDrawable(Color.TRANSPARENT))
        builder.setSpan(span, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        return builder
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ImageText

        return image == other.image
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + image.hashCode()
        return result
    }

    private class ImageSpan(private val imageDrawable: Drawable) : DynamicDrawableSpan(ALIGN_BASELINE) {

        override fun getDrawable(): Drawable {
            imageDrawable.setBounds(0, 0, imageDrawable.intrinsicWidth, imageDrawable.intrinsicHeight)
            return imageDrawable
        }
    }
}