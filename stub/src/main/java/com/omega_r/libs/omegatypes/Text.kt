package com.omega_r.libs.omegatypes

import java.io.Serializable

interface Text : Serializable, Textable {

    interface StringHolder {

        fun getStringText(): String?

    }

}