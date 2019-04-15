package com.omega_r.libs.omegatypes

import com.omega_r.libs.omegatypes.glide.GlideImage

/**
 * Created by Anton Knyazev on 15.04.19.
 */

fun Image.Companion.from(url: String) = GlideImage(url)
