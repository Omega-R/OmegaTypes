package com.omega_r.libs.omegatypes.tools

import android.graphics.Bitmap

/**
 * Created by Anton Knyazev on 2019-10-02.
 */
interface BitmapExtractor<T> {

    fun extractBitmap(source: T): Bitmap?

}